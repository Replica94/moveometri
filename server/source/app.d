import d2sqlite3;
import vibe.d;
import vibe.vibe;
import std.array;
import std.algorithm;

enum AccountLevel
{
    Unauthorized = -1,
    User = 100,
    Admin = 1000
}

struct User
{
    int id = -1;
    int level = AccountLevel.Unauthorized;
}

long getCurrentUnixMillis()
{
    return (Clock.currTime() - SysTime.fromUnixTime(0)).total!"msecs";
}

//Perform HTTP basic auth
User restAuthenticateUser(HTTPServerRequest req, HTTPServerResponse res)
{
    import vibe.http.auth.basic_auth;
    User user;
    bool do_auth(in string a, in string b)
    {
        import std.stdio;
        user = authenticateUser(a, b);
        if (user.level == AccountLevel.Unauthorized)
            return false;
        return true;
    }
    performBasicAuth(req, res, "rest", &do_auth);
    return user;
}

//Perform HTTP basic auth and allow only admin level accounts
User restAuthenticateUserAdmin(HTTPServerRequest req, HTTPServerResponse res)
{
    import vibe.http.auth.basic_auth;
    User user;
    bool do_auth(in string a, in string b)
    {
        import std.stdio;
        user = authenticateUser(a, b);
        if (user.level == AccountLevel.Admin)
            return true;
        return false;
    }
    performBasicAuth(req, res, "rest", &do_auth);
    return user;
}

interface RestIFace
{
    static class AccountFull
    {
        int id;
        string name;
        string passwordHash;
        string passwordSalt;
        long lastModification;	
        int level;
    }

    static class Account
    {
        int id;
        string name;
        long lastModification;
    }

    static class Trip
    {
        string category;
        int id;
        long timeStamp;
        double distance;
        double duration;
        long synchronizationTime;
        int accountId = 0;
    }

    static class NewTrip
    {
        int id;
    }
    @before!restAuthenticateUserAdmin("account")
    @path("/admin/accounts")
    AccountFull[] getAdminAccounts(User account);

    @before!restAuthenticateUserAdmin("account")
    @path("/admin/trips")
    Trip[] getAdminTrips(User account);

    @before!restAuthenticateUser("account")
    @queryParam("from_u", "uploadDateStart")
    @queryParam("to_u", "uploadDateEnd")
    @path("/trips")
    Trip[] getTrips(User account, long from_u = -1, long to_u = -1);


    @before!restAuthenticateUser("account")
    @path("/trips")
    NewTrip addTrip(User account, string category, long timeStamp, double distance, double duration);


    @before!restAuthenticateUser("account")
    @path("/account")
    Account getAccount(User account);

    @path("/account")
    void addNewAccount(string name, string password);

    @before!restAuthenticateUser("account")
    @path("/account/password")
    void postChangePassword(User account, string password);

    @before!restAuthenticateUser("account")
    @path("/trips/:id")
    Trip getTrip(User account, int _id);


    @before!restAuthenticateUser("account")
    @path("/trips/:id")
    void deleteTrip(User account, int _id);
}

class API : RestIFace
{

    AccountFull[] getAdminAccounts(User account)
    {
        AccountFull[] a;
        ResultRange results = db.execute("SELECT id,name,passwordHash,passwordSalt,lastModification,level FROM accounts");
        foreach (Row row; results)
        {
            AccountFull af = new AccountFull;
            af.id = row.peek!int(0);
            af.name = row.peek!string(1);
            af.passwordHash = row.peek!string(2);
            af.passwordSalt = row.peek!string(3);
            af.lastModification = row.peek!long(4);
            af.level = row.peek!int(5);
            a ~= af;
        }
        return a;
    }

    Trip[] getAdminTrips(User account)
    {
        Trip[] a;

        Statement statement = db.prepare(
            "SELECT id,category,timeStamp,distance,duration,synchronizationTime,accountId FROM trips"
        );
        
        ResultRange results = statement.execute();
        Trip gen(ref Row row)
        {
            Trip trip = tripFromRow(row);
            trip.accountId = row.peek!int(6);
            return trip;
        }
        return results.map!gen.array;
    }

    void addNewAccount(string name, string password)
    {
        try
        {
            newAccount(name, password);
        }
        catch (RestException re)
        {
            throw new HTTPStatusException(400, re.msg);
        }
    }

    void postChangePassword(User account, string password)
    {
        try
        {
            updatePassword(account, password);
        }
        catch (RestException re)
        {
            throw new HTTPStatusException(400, re.msg);
        }
    }

    NewTrip addTrip(User account, string category, long timeStamp, double distance, double duration)
    {
        Statement statement = db.prepare(
            "INSERT INTO trips (category, timeStamp, duration, distance, synchronizationTime, accountId)
            VALUES (:category, :timeStamp, :duration, :distance, :synchronizationTime, :accountId)"
        );

        statement.bind(1, category); //category
        statement.bind(2, timeStamp); //time stamp
        statement.bind(3, duration); //duration
        statement.bind(4, distance); //distance
        statement.bind(5, getCurrentUnixMillis()); //sync time
        statement.bind(6, account.id); //account id
        statement.execute();

        int rowId = cast(int)(db.lastInsertRowid);

        updateAccountLastModified(account);
        
        NewTrip n = new NewTrip;
        n.id = rowId;
        return n;
    }

    Account getAccount(User account)
    {
        Statement statement = db.prepare("SELECT id,name,lastModification FROM accounts WHERE id = :id");
        statement.bind(1, account.id);

        ResultRange results = statement.execute();

        if (results.empty())
            return null;

        auto row = results.front();
        Account t = new Account;
        t.id = row.peek!int(0);
        t.name = row.peek!string(1);
        t.lastModification = row.peek!long(2);
        return t;
    }

    static Trip tripFromRow(ref Row row, in User account = User())
    {
        Trip t = new Trip;
        t.id = row.peek!int(0);
        t.category = row.peek!string(1);
        t.timeStamp = row.peek!long(2);
        t.distance = row.peek!double(3);
        t.duration = row.peek!double(4);
        t.synchronizationTime = row.peek!long(5);
        if (account.id > 0)
            t.accountId = account.id;
        return t;
    }

    static void updateAccountLastModified(User account)
    {
        Statement updateStatement = db.prepare(
            "UPDATE accounts SET lastModification = :time WHERE id = :id"
        );

        updateStatement.bind(1, getCurrentUnixMillis());
        updateStatement.bind(2, account.id);
        updateStatement.execute();
    }

    Trip[] getTrips(User account, long from_u = -1, long to_u = -1)
    {
        Statement statement;

        if (from_u < 0 || to_u < 0)
            statement = db.prepare(
                "SELECT id,category,timeStamp,distance,duration,synchronizationTime FROM trips WHERE accountId = :id"
            );
        else
        {
            statement = db.prepare(
                "SELECT id,category,timeStamp,distance,duration,synchronizationTime FROM trips WHERE
                accountId = :id AND
                synchronizationTime BETWEEN :from AND :to"
            );

            statement.bind(2, from_u);
            statement.bind(3, to_u);
        }

        statement.bind(1, account.id);
        auto results = statement.execute();
        
        Trip r2t(ref Row t)
        {
            return tripFromRow(t, account);
        }

        return results.map!r2t.array;
    }

    Trip getTrip(User account, int id)
    {
        import std.conv;

        Statement statement = db.prepare(
            "SELECT id,category,timeStamp,distance,duration,synchronizationTime FROM trips WHERE id = :id AND accountId = :accountId"
        );
        statement.bind(1, id);
        statement.bind(2, account.id);

        ResultRange results = statement.execute();

        if (results.empty())
            return null;

        auto row = results.front();
        return tripFromRow(row, account);
    }

    void deleteTrip(User account, int id)
    {
        import std.conv;
        Statement statement = db.prepare(
            "DELETE FROM trips WHERE id = :id AND accountId = :accountId"
        );
        statement.bind(1, id);
        statement.bind(2, account.id);

        statement.execute();

        updateAccountLastModified(account);

    }
}

class RestException : Exception
{
    this(in string msg)
    {
        super(msg);
    }
}


string generatePasswordHash(in string plaintext, in string salt)
{
    import std.base64;
    import std.digest.sha;
    return Base64.encode(sha512Of(plaintext ~ salt));
}

//Returns authenticated user id or -1 on error
User authenticateUser(in string name, in string password)
{
    User user;
    Statement statement = db.prepare(
        "SELECT id, passwordHash, passwordSalt, level FROM accounts WHERE name = :name"
    );
    try
    {
        statement.bind(1, name);
        auto results = statement.execute();
        if (results.empty())
            throw new Exception("");

        auto row = results.front();
        int id = row.peek!int(0);
        string pass = row.peek!string(1);
        string salt = row.peek!string(2);
        int level = row.peek!int(3);


        if (generatePasswordHash(password, salt) == pass)
        {
            user.id = id;
            user.level = level;
        }
    }
    catch (Exception ex)
    {
    }
    return user;
}
//Updates user password
void updatePassword(in User account, in string password)
{
    import std.string;

    if (password.length < 1)
        throw new RestException("Password must not be empty");

    User user;
    Statement statement = db.prepare(
        "SELECT passwordSalt FROM accounts WHERE id = :id"
    );

    statement.bind(1, account.id);

    auto results = statement.execute();
    if (results.empty())
        throw new Exception("Failed to update account ("~account.id.to!string~") password");
    string salt = results.oneValue!string;


    string pass = generatePasswordHash(password, salt);

    Statement updateStatement = db.prepare(
        "UPDATE accounts SET passwordHash = :passwordHash WHERE id = :id"
    );

    updateStatement.bind(1, pass);
    updateStatement.bind(2, account.id);
    updateStatement.execute();
    return;
}
void newAccount(in string name, in string password, int level = AccountLevel.User)
{
    
    import std.exception;
    import std.algorithm;
    import std.ascii;
    import std.base64;

    import vibe.crypto.cryptorand;

    if (password.length < 1)
        throw new RestException("Password must not be empty");

    if (! name.all!isAlphaNum || name.length < 1)
        throw new RestException("Username must be at least 1 character long and must only contain alphanumeric characters");

    auto rng = secureRNG();
    ubyte[8] saltBytes;
    rng.read(saltBytes);

    Statement statement = db.prepare(
                "INSERT INTO accounts (name, passwordHash, passwordSalt, lastModification, level)
                VALUES (:name, :passwordHash, :passwordSalt, :lastModification, :level)"
        );

    string salt = Base64.encode(saltBytes);
    string hash = generatePasswordHash(password, salt);

    //Name
    statement.bind(1, name);
    //Password hash
    statement.bind(2, hash);
    //Salt
    statement.bind(3, salt);
    //Last modification
    statement.bind(4, getCurrentUnixMillis());
    //Account level
    statement.bind(5, level);

    try
    {
        statement.execute();
    }
    catch (Exception ex)
    {
        throw new RestException("Username already taken");
    }
}

Database db;

shared static this()
{
    import std.stdio;
    db = Database("db");

    db.run("CREATE TABLE IF NOT EXISTS trips (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            category  TEXT NOT NULL,
            timeStamp INTEGER NOT NULL,
            distance REAL NOT NULL,
            duration REAL NOT NULL,
            synchronizationTime INTEGER NOT NULL,
            accountId INTEGER  NOT NULL
        )");

    db.run("CREATE TABLE IF NOT EXISTS accounts (
            id    INTEGER PRIMARY KEY AUTOINCREMENT,
            name  TEXT NOT NULL UNIQUE,
            passwordHash TEXT NOT NULL,
            passwordSalt TEXT NOT NULL,
            lastModification INTEGER NOT NULL,
            level INTEGER NOT NULL
        )");


    import std.datetime; 

    auto router = new URLRouter;
    router.registerRestInterface(new API());
    auto settings = new HTTPServerSettings;
    settings.port = 5511;

    listenHTTP(settings, router);
}

void main(string[] asd)
{
    if (!finalizeCommandLineOptions())
            return;
    lowerPrivileges();
    runEventLoop();
}
