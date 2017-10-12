import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import fr.enseeiht.danck.voice_analyzer.*;

public final class SQliteConnector implements Connector {

	private Connection c;
    private Statement stmt;

    SQliteConnector(String path) {
        try {
            Class.forName("org.sqlite.JDBC");
            this.c = DriverManager.getConnection("jdbc:sqlite:" + path);
            this.stmt = this.c.createStatement();

            this.stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS meta_token " +
                            "(id      INTEGER PRIMARY KEY AUTOINCREMENT," +
                            " token   TEXT    NOT NULL," +
                            " speaker INTEGER NOT NULL," +
                            " data    TEXT    NOT NULL    UNIQUE)"
            );

            this.c.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(0);
        }
    }
    
	@Override
	public void create(MetaToken metaToken) {
		try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(metaToken);
            out.flush();
            String data = DatatypeConverter.printBase64Binary(bos.toByteArray());
            this.stmt.executeUpdate(
                    "INSERT INTO meta_token (token, speaker, data) " +
                            "VALUES ('" + metaToken.getToken().name() + "', '"
                            + metaToken.getSpeaker() + "', '"
                            + data + "');"
            );
            this.c.commit();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(0);
        }
	}

	@Override
	public MetaToken[] getList(Map<String, String> filters) {
		System.out.println("");
        System.out.println(filters.toString());

        // Create the SQlite inclusion filters
        StringBuilder conditional = new StringBuilder();
        if (filters.size() != 0) {
            conditional.append("WHERE ");
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                conditional.append(entry.getKey());
                String[] choices = entry.getValue().split("\\|");
                if (choices.length > 1) {
                    conditional.append(" IN (");
                    for (String choice : choices) {
                        conditional.append("'").append(choice).append("', ");
                    }
                    conditional = new StringBuilder(conditional.substring(0, conditional.length() - 2) + ")");
                } else {
                    conditional.append(" = '").append(entry.getValue()).append("'");
                }
                conditional.append(" AND ");
            }
            conditional = new StringBuilder(conditional.substring(0, conditional.length() - 5));
        }

        // Fetch in DB & reconstruct MetaTokens
        ArrayList<MetaToken> metaTokens = new ArrayList<>();
        try {
            ResultSet rs = this.stmt.executeQuery(
                    "SELECT data FROM meta_token " + conditional + ";"
            );
            while (rs.next()) {
                String data = rs.getString("data");
                byte[] bytes = DatatypeConverter.parseBase64Binary(data);
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInput in = new ObjectInputStream(bis);
                MetaToken metaToken = (MetaToken) in.readObject();
                metaTokens.add(metaToken);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(0);
        }

        return metaTokens.toArray(new MetaToken[0]);
    }
	
	
}
