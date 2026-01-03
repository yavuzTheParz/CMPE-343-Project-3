package main.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import main.DatabaseAdapter;
import main.models.Message;
import main.models.MessageThread;

public class MessageDAO {

    public static MessageThread ensureThread(String customer, String owner) {
        String insert = "INSERT IGNORE INTO threads (customer, owner) VALUES (?, ?)";
        String select = "SELECT id, customer, owner FROM threads WHERE customer = ? AND owner = ?";
        try (Connection conn = DatabaseAdapter.getConnection()) {
            try (PreparedStatement p = conn.prepareStatement(insert)) { p.setString(1, customer); p.setString(2, owner); p.executeUpdate(); }
            try (PreparedStatement p2 = conn.prepareStatement(select)) {
                p2.setString(1, customer); p2.setString(2, owner);
                ResultSet rs = p2.executeQuery();
                if (rs.next()) {
                    MessageThread t = new MessageThread();
                    t.setId(rs.getInt("id"));
                    t.setCustomer(rs.getString("customer"));
                    t.setOwner(rs.getString("owner"));
                    return t;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static boolean addMessage(Message m) {
        String sql = "INSERT INTO messages (thread_id, sender, content, is_read) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseAdapter.getConnection(); PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, m.getThreadId());
            pst.setString(2, m.getSender());
            pst.setString(3, m.getContent());
            pst.setBoolean(4, m.isRead());
            int affected = pst.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            // Primary (new) schema insert failed — attempt legacy fallback for older DBs
            try (Connection conn = DatabaseAdapter.getConnection()) {
                // Legacy schema likely has columns: sender_username, content, sent_at (timestamp default)
                String legacy = "INSERT INTO messages (sender_username, content) VALUES (?, ?)";
                try (PreparedStatement pst2 = conn.prepareStatement(legacy, Statement.RETURN_GENERATED_KEYS)) {
                    pst2.setString(1, m.getSender());
                    pst2.setString(2, m.getContent());
                    int affected2 = pst2.executeUpdate();
                    if (affected2 > 0) {
                        // try to attach this legacy message to the thread_id if possible
                        try (ResultSet gk = pst2.getGeneratedKeys()) {
                            if (gk != null && gk.next()) {
                                int newId = gk.getInt(1);
                                if (m.getThreadId() > 0) {
                                    try (PreparedStatement upd = conn.prepareStatement("UPDATE messages SET thread_id = ? WHERE id = ?")) {
                                        upd.setInt(1, m.getThreadId());
                                        upd.setInt(2, newId);
                                        upd.executeUpdate();
                                    } catch (SQLException ignore) {
                                        // if update fails (no thread_id column), ignore
                                    }
                                }
                            }
                        } catch (SQLException ignore) {}
                    }
                    return affected2 > 0;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public static List<Message> getMessagesForThread(int threadId) {
        List<Message> list = new ArrayList<>();
        String sql1 = "SELECT id, thread_id, sender, content, is_read FROM messages WHERE thread_id = ? ORDER BY created_at ASC";
        String sql2 = "SELECT id, thread_id, sender_username AS sender, content, FALSE AS is_read, sent_at AS created_at FROM messages WHERE thread_id = ? ORDER BY sent_at ASC";
        try (Connection conn = DatabaseAdapter.getConnection()) {
            // try primary (new schema)
            try (PreparedStatement pst = conn.prepareStatement(sql1)) {
                pst.setInt(1, threadId);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        Message m = new Message();
                        m.setId(rs.getInt("id"));
                        m.setThreadId(rs.getInt("thread_id"));
                        m.setSender(rs.getString("sender"));
                        try { m.setContent(rs.getString("content")); } catch (SQLException ex) { m.setContent(""); }
                        try { m.setRead(rs.getBoolean("is_read")); } catch (SQLException ex) { m.setRead(false); }
                        list.add(m);
                    }
                    return list;
                }
            } catch (SQLException e1) {
                // primary failed (likely missing 'sender' column) — try legacy schema
                try (PreparedStatement pst2 = conn.prepareStatement(sql2)) {
                    pst2.setInt(1, threadId);
                    try (ResultSet rs2 = pst2.executeQuery()) {
                        while (rs2.next()) {
                            Message m = new Message();
                            m.setId(rs2.getInt("id"));
                            try { m.setThreadId(rs2.getInt("thread_id")); } catch (SQLException ex) { m.setThreadId(threadId); }
                            // legacy column aliased as 'sender'
                            m.setSender(rs2.getString("sender"));
                            m.setContent(rs2.getString("content"));
                            m.setRead(false);
                            list.add(m);
                        }
                        return list;
                    }
                } catch (SQLException e2) {
                    e2.printStackTrace();
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<MessageThread> getThreadsForOwner(String owner) {
        List<MessageThread> list = new ArrayList<>();
        String sql = "SELECT id, customer, owner, created_at FROM threads WHERE owner = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseAdapter.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, owner);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                MessageThread t = new MessageThread();
                t.setId(rs.getInt("id"));
                t.setCustomer(rs.getString("customer"));
                t.setOwner(rs.getString("owner"));
                list.add(t);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<MessageThread> getThreadsForCustomer(String customer) {
        List<MessageThread> list = new ArrayList<>();
        String sql = "SELECT id, customer, owner, created_at FROM threads WHERE customer = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseAdapter.getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, customer);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                MessageThread t = new MessageThread();
                t.setId(rs.getInt("id"));
                t.setCustomer(rs.getString("customer"));
                t.setOwner(rs.getString("owner"));
                list.add(t);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

}
