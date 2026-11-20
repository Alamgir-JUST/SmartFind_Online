package edu.smartfind.dao;

import edu.smartfind.db.Database;
import edu.smartfind.model.Item;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** JDBC persistence layer: students (1) -> (many) items. */
public class ItemDAO {
    private static final String BASE = "SELECT i.*, s.full_name, s.email FROM items i JOIN students s ON s.student_id=i.student_id ";
    private Item read(ResultSet rs) throws SQLException {
        return new Item(rs.getLong("item_id"),rs.getLong("student_id"),rs.getString("full_name"),rs.getString("email"),
            rs.getString("item_type"),rs.getString("item_name"),rs.getString("category"),rs.getString("color"),
            rs.getString("location"),rs.getString("description"),rs.getDate("incident_date").toLocalDate(),rs.getString("status"));
    }
    public List<Item> all(String search, String type) throws SQLException {
        String sql = BASE + "WHERE (?='' OR i.item_name LIKE ? OR i.description LIKE ? OR i.location LIKE ? OR s.full_name LIKE ?) " +
                "AND (?='' OR i.item_type=?) ORDER BY i.item_id DESC";
        String pattern = "%" + search + "%";
        try (Connection c=Database.connect(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,search); for (int i=2;i<=5;i++) ps.setString(i,pattern);
            ps.setString(6,type); ps.setString(7,type);
            try(ResultSet rs=ps.executeQuery()){List<Item> items=new ArrayList<>();while(rs.next()) items.add(read(rs));return items;}
        }
    }
    public Item byId(long id) throws SQLException {
        try(Connection c=Database.connect(); PreparedStatement ps=c.prepareStatement(BASE+"WHERE i.item_id=?")) {
            ps.setLong(1,id);try(ResultSet rs=ps.executeQuery()){return rs.next()?read(rs):null;}
        }
    }
    public void insert(String owner,String email,String type,String name,String category,String color,String location,String desc,LocalDate date) throws SQLException {
        try(Connection c=Database.connect()) {
            c.setAutoCommit(false);
            try {
                // ON DUPLICATE ensures one student per email, returning the existing/new ID.
                long studentId;
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO students(full_name,email) VALUES (?,?) ON DUPLICATE KEY UPDATE student_id=LAST_INSERT_ID(student_id), full_name=VALUES(full_name)")) {
                    ps.setString(1,owner);ps.setString(2,email);ps.executeUpdate();
                }
                try(Statement s=c.createStatement();ResultSet rs=s.executeQuery("SELECT LAST_INSERT_ID()")) {rs.next();studentId=rs.getLong(1);}
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO items(student_id,item_type,item_name,category,color,location,description,incident_date) VALUES (?,?,?,?,?,?,?,?)")) {
                    ps.setLong(1,studentId);ps.setString(2,type);ps.setString(3,name);ps.setString(4,category);ps.setString(5,color);
                    ps.setString(6,location);ps.setString(7,desc);ps.setDate(8,java.sql.Date.valueOf(date));ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {c.rollback(); throw e;} finally {c.setAutoCommit(true);}
        }
    }
    public boolean update(long id,String type,String name,String category,String color,String location,String desc,LocalDate date,String status) throws SQLException {
        try(Connection c=Database.connect();PreparedStatement ps=c.prepareStatement("UPDATE items SET item_type=?,item_name=?,category=?,color=?,location=?,description=?,incident_date=?,status=? WHERE item_id=?")) {
            ps.setString(1,type);ps.setString(2,name);ps.setString(3,category);ps.setString(4,color);ps.setString(5,location);ps.setString(6,desc);
            ps.setDate(7,java.sql.Date.valueOf(date));ps.setString(8,status);ps.setLong(9,id);return ps.executeUpdate()>0;
        }
    }
    public boolean delete(long id) throws SQLException {
        try(Connection c=Database.connect();PreparedStatement ps=c.prepareStatement("DELETE FROM items WHERE item_id=?")) {ps.setLong(1,id);return ps.executeUpdate()>0;}
    }
}
