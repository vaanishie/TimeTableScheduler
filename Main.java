public class Main {

    public static void main(String[] args) {

        // Initialise the database (creates tables + seeds data if first run)
        db.DatabaseManager.getInstance().initialise();

        // Launch the Admin Dashboard on Swing's Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(() ->
            new ui.AdminDashboard().show()
        );
    }
}
