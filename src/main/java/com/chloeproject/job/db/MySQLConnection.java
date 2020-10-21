package com.chloeproject.job.db;

import com.chloeproject.job.entity.Item;
import com.chloeproject.job.env.MySQLDBUtil;

import java.sql.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MySQLConnection {

    private Connection conn; // connection to MySQL on AWS RDS

    /**
     * Constructor
     */
    public MySQLConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(MySQLDBUtil.URL);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Close connection to database
     */
    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save item to "items" table and its corresponding keywords in "keywords" table
     * in database using INSERT query (SQL string).
     *
     * @param item Item object
     */
    public void saveItem(Item item) {
        // sanity check for connection to database
        if (conn == null) {
            System.err.println("DB connection failed");
            return;
        }

        // try insert item to database's "items" table, and catch exception if any
        String insertItemSql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(insertItemSql);
            statement.setString(1, item.getId());
            statement.setString(2, item.getTitle());
            statement.setString(3, item.getLocation());
            statement.setString(4, item.getCompanyLogo());
            statement.setString(5, item.getUrl());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // try insert keyword of the items to database's "keywords" table, and catch exception if any
        String insertKeywordSql = "INSERT IGNORE INTO keywords VALUES (?, ?)"; // first ? is item_id, second ? is keyword_item）
        try {
            // iterate the list keywords (maximum 3 keywords)
            for (String keyword : item.getKeywords()) {
                PreparedStatement statement = conn.prepareStatement(insertKeywordSql);
                statement = conn.prepareStatement(insertKeywordSql);
                statement.setString(1, item.getId());
                statement.setString(2, keyword);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save user's favorite items to "history" table in database, by calling
     * saveItem() helper method.
     *
     * @param userId user's ID
     * @param item Item object
     */
    public void setFavoriteItems(String userId, Item item) {
        // sanity check for connection to database
        if (conn == null) {
            System.err.println("DB connection failed");
            return;
        }

        // save the item into "items" table and its corresponding keywords into "keywords" table
        saveItem(item);

        // try insert the item_id relating the user_id to the database's "history" table,
        // and catch exception is any
        String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, item.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unsave/Delete user's favorite items from "history" table, by using DELETE query.
     *
     * @param userId user's ID
     * @param itemId id of item
     */
    public void unsetFavoriteItems(String userId, String itemId) {
        // sanity check for connection to database
        if (conn == null) {
            System.err.println("DB connection failed");
            return;
        }

        // try delete the item_id relating the user_id from database's "history" table,
        // and catch exception if any
        String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, itemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads data from the "history" table using SELECT query.
     *
     * @param userId
     * @return a set of user's historical favorite items
     */
    public Set<String> getFavoriteItemIds(String userId) {
        // sanity check for connection
        if (conn == null) {
            System.err.println("DB connection failed");
            return new HashSet<>();
        }

        // try read/select the item_id relating the user_id from database's "history" table,
        // and store them in a set, catch exception if any
        Set<String> favoriteItems = new HashSet<>();
        try {
            String sql = "SELECT item_id FROM history WHERE user_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String itemId = rs.getString("item_id");
                favoriteItems.add(itemId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favoriteItems;
    }

    /**
     * Read detailed data of the favorite items of a user.
     *
     * @param userId user's ID
     * @return  a set of favorite items of the user
     */
    public Set<Item> getFavoriteItems(String userId) {
        // sanity check for connection
        if (conn == null) {
            System.err.println("DB connection failed");
            return new HashSet<>();
        }

        // try read each item id of the set of favoriteItemIds from database's "items" table,
        // and store them in a set (favoriteItems), catch exception if any
        Set<Item> favoriteItems = new HashSet<>();
        Set<String> favoriteItemIds = getFavoriteItemIds(userId);

        String sql = "SELECT * FROM items WHERE item_id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            for (String itemId : favoriteItemIds) {
                statement.setString(1, itemId);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    favoriteItems.add(new Item(rs.getString("item_id")
                            ,rs.getString("name")
                            ,rs.getString("address")
                            ,rs.getString("image_url")
                            ,rs.getString("url")
                            ,null
                            , getKeywords(itemId)
                            ,true));

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favoriteItems;
    }

    /**
     * Read the detail information (keywords of a given item id) using
     * SELECT query.
     *
     * @param itemId
     * @return a set of keywords of the corresponding itemId
     */
    public Set<String> getKeywords(String itemId) {
        // sanity check for connection
        if (conn == null) {
            System.err.println("DB connection failed");
            return Collections.emptySet();
        }

        // try read the keywords of the corresponding itemId from database's "keywords" table,
        // and store them in a set (keywords), catch exception if any
        Set<String> keywords = new HashSet<>();
        String sql = "SELECT keyword from keywords WHERE item_id = ? ";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, itemId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String keyword = rs.getString("keyword");
                keywords.add(keyword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return keywords;
    }

    /**
     * Read the full name of the user given the userId,
     * using SELECT query.
     *
     * @param userId
     * @return a string of full name of the user
     */
    public String getFullname(String userId) {
        // sanity check for connection
        if (conn == null) {
            System.err.println("DB connection failed");
            return "";
        }

        // try read the full name of the userId from database's "users" table,
        // and catch exception if any
        String name = "";
        String sql = "SELECT first_name, last_name FROM users WHERE user_id = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                name = rs.getString("first_name") + " " + rs.getString("last_name");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return name;
    }

    /**
     * Verify the login information by checking if the userId and password matches
     * the data from database.
     * @param userId
     * @param password
     * @return true if the userId and password matches, false otherwise
     */
    public boolean verifyLogin(String userId, String password) {
        // sanity check for connection
        if (conn == null) {
            System.err.println("DB connection failed");
            return false;
        }

        // try read the full name of the userId and password from database's "users" table,
        // validate the login information and catch exception if any
        String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    /**
     * Add user's information to database's "users" table when a new user registers.
     * @param userId
     * @param password
     * @param firstname
     * @param lastname
     * @return true if adding user's information successfully to database, false otherwise
     */
    public boolean addUser(String userId, String password, String firstname, String lastname) {
        // sanity check for connection
        if (conn == null) {
            System.err.println("DB connection failed");
            return false;
        }

        // try insert the full name of the userId and password to database's "users" table,
        // and catch exception if any
        String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            statement.setString(2, password);
            statement.setString(3, firstname);
            statement.setString(4, lastname);

            return statement.executeUpdate() == 1; // row count == 1 means successfully executed and updated in database
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
