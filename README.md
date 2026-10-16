# OASIS INFOBYTE — Java Development Task: Online Reservation System

A desktop application using Java Swing, JDBC, and SQLite. It includes account registration/login, booking, PNR generation, reservation listing, and cancellation.

## Requirements
- JDK 17 or newer
- Apache Maven 3.8+ (Maven downloads the SQLite JDBC library automatically)
- Internet access for the first Maven build only

## Run in VS Code / terminal
1. Open this project folder (the folder containing `pom.xml`).
2. Confirm Java and Maven are installed: `java -version` and `mvn -version`.
3. Run:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.oasis.reservation.OnlineReservationApp"
```

If the `exec:java` command says the exec plugin is unavailable, run it with:

```bash
mvn org.codehaus.mojo:exec-maven-plugin:3.3.0:java -Dexec.mainClass="com.oasis.reservation.OnlineReservationApp"
```

4. In the app, enter a username of at least 3 characters and password of at least 4 characters, then click **Create account**.
5. Log in using the same credentials.
6. Use **Book Reservation** to enter passenger/train/journey details. The date must be `YYYY-MM-DD` and cannot be in the past.
7. A PNR appears after booking. Open **My Reservations / Cancel** to see bookings, refresh the list, or cancel a selected booking.
8. The database file `reservations.db` is created automatically in the working directory.

## Notes
- This is an educational internship project. Passwords are stored as plain text to keep the sample focused; do not use this design for a real service. A production system should use a secure password-hashing algorithm and stronger validation.
- The app records reservation details locally. It does not connect to a real railway booking provider or verify actual train schedules.
