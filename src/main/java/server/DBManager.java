package server;

import common.data.*;
import common.network.CommandResult;
import common.network.Request;

import java.nio.file.AccessDeniedException;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class DBManager {
    private static final String TABLE_USER = "\"table_user\"";
    private static final String TABLE_PERSON = "person";

    private static final String USER_ID = "id";
    private static final String USERNAME = "\"username\"";
    private static final String PASSWORD = "\"password\"";

    private static final String PERSON_ID = "id";
    private static final String NAME = "name";
    private static final String COORDINATE_X = "coordinate_x";
    private static final String COORDINATE_Y = "coordinate_y";
    private static final String CREATION_DATE = "creation_date";
    private static final String COUNTRY = "nationality";
    private static final String HEIGHT = "height";
    private static final String EYE_COLOR = "eye_color";
    ;
    private static final String LOCATION_X = "location_x";
    private static final String LOCATION_Y = "location_y";
    private static final String LOCATION_NAME = "location_name";
    private static final String HAIR_COLOR = "hair_color";
    private static final String OWNER_USERNAME = "owner_username";

    private static final String SQL_ADD_USER = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)",
            TABLE_USER, USERNAME, PASSWORD);

    private static final String SQL_FIND_USERNAME = String.format("SELECT COUNT(*) FROM %s WHERE %s = ?",
            TABLE_USER, USERNAME);
    private static final String SQL_CHECK_USER = String.format("SELECT COUNT(*) FROM %s WHERE %s = ? AND %s = ?",
            TABLE_USER, USERNAME, PASSWORD);
    private static final String SQL_CHECK_USER1 = String.format("SELECT COUNT(*) FROM %s WHERE %s = ?",
            TABLE_USER, USERNAME);
    private static final String SQL_ADD_PERSON = String.format("INSERT INTO %s (" +
                    "%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING %s",
            TABLE_PERSON,
            NAME, COORDINATE_X, COORDINATE_Y, CREATION_DATE, HEIGHT, EYE_COLOR, HAIR_COLOR, COUNTRY,
            LOCATION_X, LOCATION_Y, LOCATION_NAME, OWNER_USERNAME, PERSON_ID);
    private static final String SQL_REMOVE_BY_ID = String.format("DELETE FROM %s WHERE %s = ?",
            TABLE_PERSON, PERSON_ID);
    private static final String SQL_UPDATE_BY_ID = String.format("UPDATE %s SET " +
                    "%s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?" +
                    "WHERE %s = ?",
            TABLE_PERSON,
            NAME, COORDINATE_X, COORDINATE_Y, HEIGHT, EYE_COLOR, HAIR_COLOR, COUNTRY,
            LOCATION_X, LOCATION_Y, LOCATION_NAME, PERSON_ID);
    private static final String SQL_ID_EXIST = String.format("SELECT COUNT(*) as count FROM %s WHERE %s = ?",
            TABLE_PERSON, PERSON_ID);
    private static final String SQL_BELONGS_USER = String.format("SELECT %s FROM %s WHERE %s = ?", OWNER_USERNAME, TABLE_PERSON, PERSON_ID);
    private static final String SQL_GET_PERSON = String.format("SELECT * FROM %s", TABLE_PERSON);

    private final String url;
    private final String username;
    private final String password;
    private Connection connection;

    public DBManager(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * connect to databases with driver manager
     */
    public void connectDB() {
        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Подключение к базе данных установлено.");
        } catch (SQLException e) {
            System.out.println("Не удалось выполнить подключение к базе данных.");
            System.exit(-1);
        }
    }

    public boolean checkUser(User user) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_CHECK_USER);
        statement.setString(1, user.getUsername());
        statement.setString(2, user.getPassword());
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        int count = resultSet.getInt(1);
        statement.close();
        return count != 0;
    }

    public boolean checkUsername(String login) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_CHECK_USER1);
        statement.setString(1, login);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        int count = resultSet.getInt(1);
        statement.close();
        return count != 0;
    }

    public CommandResult checkLogin(Request<?> request) {
        try {
            String login = (String) request.type;
            if (checkUsername(login)) {
                return new CommandResult(true, "Добро пожаловать");
            }
            return new CommandResult(false, "Неверный логин");
        } catch (SQLException exception) {
            return new CommandResult(false, "SQL-ошибка на сервере");
        } catch (Exception exception) {
            return new CommandResult(false, "Аргумент другого типа");
        }
    }

    public CommandResult login(Request<?> request) {
        try {
            User user = (User) request.type;
            if (checkUser(user)) {
                return new CommandResult(true, "Добро пожаловать");
            }
            return new CommandResult(false, "Неверный пароль");
        } catch (SQLException exception) {
            return new CommandResult(false, "SQL-ошибка на сервере");
        } catch (Exception exception) {
            return new CommandResult(false, "Аргумент другого типа");
        }
    }

    public CommandResult register(Request<?> request) {
        try {
            User user = (User) request.type;

            if (!userExists(user.getUsername())) {
                registerUser(user);
                return new CommandResult(true,
                        "Добро пожаловать");
            }
            return new CommandResult(false, "Такое имя уже используется");
        } catch (SQLException exception) {
            return new CommandResult(false, "SQL-ошибка на сервере");
        } catch (Exception exception) {
            return new CommandResult(false, "Аргумент другого типа");
        }
    }

    public CommandResult checkRegister(Request<?> request) {
        try {
            String login = (String) request.type;

            if (!userExists(login)) {
                return new CommandResult(true,
                        "Имя пользователя доступно");
            }
            return new CommandResult(false, "Такое имя уже используется");
        } catch (SQLException exception) {
            return new CommandResult(false, "SQL-ошибка на сервере");
        } catch (Exception exception) {
            return new CommandResult(false, "Аргумент другого типа");
        }
    }

    public boolean userExists(String username) throws SQLException {
        System.out.println(SQL_FIND_USERNAME);
        PreparedStatement statement = connection.prepareStatement(SQL_FIND_USERNAME);
        statement.setString(1, username);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        int count = resultSet.getInt(1);
        statement.close();
        return count != 0;
    }

    public void registerUser(User user) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_ADD_USER);
        statement.setString(1, user.getUsername());
        statement.setString(2, user.getPassword());
        statement.executeUpdate();
        statement.close();
    }

    public ConcurrentSkipListSet<Person> readCollection() {
        ConcurrentSkipListSet<Person> collection = new ConcurrentSkipListSet<>();
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_GET_PERSON);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Person person = getPersonFromResult(resultSet);
                collection.add(person);
            }

            statement.close();
            System.out.println("Коллекция загружена из базы данных");
        } catch (SQLException exception) {
            System.out.println("Ошибка при загрузке коллекции");
            exception.printStackTrace();
            AtomicBoolean atomicBoolean = new AtomicBoolean(false);
            atomicBoolean.set(true);
        }
        return collection;
    }

    private Person getPersonFromResult(ResultSet resultSet) throws SQLException {
        return new Person(
                resultSet.getInt(PERSON_ID),
                resultSet.getString(NAME),
                new Coordinates(
                        resultSet.getLong(COORDINATE_X),
                        resultSet.getInt(COORDINATE_Y)
                ),
                resultSet.getInt(HEIGHT),
                Color.valueOf(resultSet.getString(EYE_COLOR)),
                Color.valueOf(resultSet.getString(HAIR_COLOR)),
                Country.valueOf(resultSet.getString(COUNTRY)),
                new Location(
                        resultSet.getDouble(LOCATION_X),
                        resultSet.getFloat(LOCATION_Y),
                        resultSet.getString(LOCATION_NAME)

                ),
                resultSet.getString(OWNER_USERNAME)
        );
    }

    private int prepareStatement(PreparedStatement statement, Person person, boolean changeDate) throws SQLException {
        Coordinates coordinates = person.getCoordinates();
        ZonedDateTime creationDate = person.getCreationDate();
        Country nationality = person.getNationality();
        Color eyeColor = person.getEyeColor();
        Color hairColor = person.getHairColor();
        Location location = person.getLocation();

        int i = 1;
        statement.setString(i++, person.getName());
        statement.setLong(i++, coordinates.getX());
        statement.setInt(i++, coordinates.getY());
        if (changeDate)
            statement.setTimestamp(i++, Timestamp.from(creationDate.toInstant()));
        statement.setInt(i++, person.getHeight());
        statement.setString(i++, eyeColor.name());
        statement.setString(i++, hairColor.name());
        statement.setString(i++, nationality.name());
        statement.setDouble(i++, location.getX());
        statement.setFloat(i++, location.getY());
        statement.setString(i++, location.getLocationName());

        return i;
    }

    public boolean addPerson(Person person, String owner) {
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_ADD_PERSON,
                    Statement.RETURN_GENERATED_KEYS);
            int i = prepareStatement(statement, person, true);
            statement.setString(i, owner);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                throw new SQLException("Нет измененных строк");
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next())
                    person.setId(generatedKeys.getInt(PERSON_ID));
                else
                    throw new SQLException("ID не получено");
            }
            statement.close();
            return true;
        } catch (SQLException exception) {
            System.out.println("Ошибка sql");

        }

        return false;
    }

    public boolean removeById(int id, String username) throws SQLException {
        if (!existId(id)) {
            System.out.println("Данного ID не существует");
            return false;
        }
        if (!belongsToUser(id, username)) {
            System.out.println("Вы не можете удалить данного персонажа");
            return false;
        }

        PreparedStatement statement = connection.prepareStatement(SQL_REMOVE_BY_ID);
        statement.setInt(1, id);
        statement.executeUpdate();

        return true;
    }

    public boolean existId(int id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_ID_EXIST);
        statement.setInt(1, id);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1) != 0;
    }

    public boolean belongsToUser(int id, String username) throws SQLException {
        if (!existId(id)) {
            return false;
        }

        PreparedStatement statement = connection.prepareStatement(SQL_BELONGS_USER);
        statement.setInt(1, id);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        String owner = resultSet.getString(OWNER_USERNAME);

        return username.equals(owner);
    }

    private List<Integer> getsID(List<Map.Entry<Integer, String>> list) throws SQLException {
        List<Integer> deletedID = removeAndGetIds(list);
        return deletedID;
    }

    private List<Integer> removeAndGetIds(List<Map.Entry<Integer, String>> list) throws SQLException {
        List<Integer> deletedID = new ArrayList<>();
        for (Map.Entry<Integer, String> person : list) {
            boolean status = removeById(person.getKey(), username);
            if (status) {
                deletedID.add(person.getKey());
            }
        }
        return deletedID;
    }

    public boolean deleteAllOwned(String username) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + TABLE_PERSON
                    + " WHERE owner_username =?");
            statement.setString(1, username);
            statement.execute();
            ResultSet resultSet = statement.executeQuery();
            return !resultSet.next();
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updatePerson(int id, Person person, String username) throws SQLException, AccessDeniedException {
        if (!existId(id)) {
            return false;
        }
        if (!belongsToUser(id, username)) {
            return false;
        }
        try {
            PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_BY_ID);
            int i = prepareStatement(statement, person, false);
            statement.setInt(i, id);
            statement.executeUpdate();
            statement.close();
            return true;
        } catch (SQLException exception) {
            System.out.println("Ошибка sql");
            return false;
        }
    }
}
