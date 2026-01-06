import java.util.*;
import java.time.*;
public class LibrarySystem {

    // -------------------- Domain classes --------------------

    static class Book {
        private final String id;
        private String title;
        private String author;
        private int totalCopies;
        private int availableCopies;

        public Book(String id, String title, String author, int totalCopies) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.totalCopies = totalCopies;
            this.availableCopies = totalCopies;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public int getTotalCopies() {
            return totalCopies;
        }

        public int getAvailableCopies() {
            return availableCopies;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public void setTotalCopies(int totalCopies) {
            int diff = totalCopies - this.totalCopies;
            this.totalCopies = totalCopies;
            this.availableCopies += diff;
            if (this.availableCopies < 0) {
                this.availableCopies = 0;
            }
        }

        public void decreaseAvailable() {
            if (availableCopies > 0) {
                availableCopies--;
            }
        }

        public void increaseAvailable() {
            if (availableCopies < totalCopies) {
                availableCopies++;
            }
        }

        @Override
        public String toString() {
            return "Book{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    ", author='" + author + '\'' +
                    ", totalCopies=" + totalCopies +
                    ", availableCopies=" + availableCopies +
                    '}';
        }
    }

    static class User {
        private final String id;
        private final String name;
        private final boolean isLibrarian;

        public User(String id, String name, boolean isLibrarian) {
            this.id = id;
            this.name = name;
            this.isLibrarian = isLibrarian;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public boolean isLibrarian() {
            return isLibrarian;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", isLibrarian=" + isLibrarian +
                    '}';
        }
    }

    static class Reservation {
        private static long COUNTER = 1;

        private final long reservationId;
        private final String userId;
        private final String bookId;
        private final Instant createdAt;

        public Reservation(String userId, String bookId) {
            this.reservationId = COUNTER++;
            this.userId = userId;
            this.bookId = bookId;
            this.createdAt = Instant.now();
        }

        public long getReservationId() {
            return reservationId;
        }

        public String getUserId() {
            return userId;
        }

        public String getBookId() {
            return bookId;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public boolean isExpired(Duration ttl) {
            Instant now = Instant.now();
            return createdAt.plus(ttl).isBefore(now);
        }

        @Override
        public String toString() {
            return "Reservation{" +
                    "reservationId=" + reservationId +
                    ", userId='" + userId + '\'' +
                    ", bookId='" + bookId + '\'' +
                    ", createdAt=" + createdAt +
                    '}';
        }
    }

    static class Loan {
        private static long COUNTER = 1;

        private final long loanId;
        private final String userId;
        private final String bookId;
        private final Instant borrowedAt;
        private Instant returnedAt;

        public Loan(String userId, String bookId) {
            this.loanId = COUNTER++;
            this.userId = userId;
            this.bookId = bookId;
            this.borrowedAt = Instant.now();
        }

        public long getLoanId() {
            return loanId;
        }

        public String getUserId() {
            return userId;
        }

        public String getBookId() {
            return bookId;
        }

        public Instant getBorrowedAt() {
            return borrowedAt;
        }

        public Instant getReturnedAt() {
            return returnedAt;
        }

        public void markReturned() {
            this.returnedAt = Instant.now();
        }

        public boolean isActive() {
            return returnedAt == null;
        }

        @Override
        public String toString() {
            return "Loan{" +
                    "loanId=" + loanId +
                    ", userId='" + userId + '\'' +
                    ", bookId='" + bookId + '\'' +
                    ", borrowedAt=" + borrowedAt +
                    ", returnedAt=" + returnedAt +
                    '}';
        }
    }

    // -------------------- Library Service --------------------

    static class Library {
        private final Map<String, Book> books = new HashMap<>();
        private final Map<String, User> users = new HashMap<>();
        private final List<Reservation> reservations = new ArrayList<>();
        private final List<Loan> loans = new ArrayList<>();

        // 48 hours expiry for reservations
        private final Duration RESERVATION_TTL = Duration.ofHours(48);
        // Max number of borrowed books per user
        private final int MAX_BORROWED_PER_USER = 5;

        public Library() {
            seedData();
        }

        private void seedData() {
            // Sample books
            addBook(new Book("B001", "Clean Code", "Robert C. Martin", 3));
            addBook(new Book("B002", "Effective Java", "Joshua Bloch", 2));
            addBook(new Book("B003", "Design Patterns", "GoF", 1));

            // Sample users
            addUser(new User("U001", "Harika Student", false));
            addUser(new User("U002", "Pranay Student", false));
            addUser(new User("L001", "Malli Librarian", true));
        }

        // ---------- User & Book Management ----------

        public void addUser(User user) {
            users.put(user.getId(), user);
        }

        public User getUser(String userId) {
            return users.get(userId);
        }

        public void addBook(Book book) {
            books.put(book.getId(), book);
        }

        public Book getBook(String bookId) {
            return books.get(bookId);
        }

        public Collection<Book> getAllBooks() {
            return books.values();
        }

        public Collection<User> getAllUsers() {
            return users.values();
        }

        // ---------- Search ----------

        public List<Book> searchBooks(String query) {
            List<Book> result = new ArrayList<>();
            String q = query.toLowerCase(Locale.ROOT);
            for (Book book : books.values()) {
                if (book.getTitle().toLowerCase(Locale.ROOT).contains(q) ||
                        book.getAuthor().toLowerCase(Locale.ROOT).contains(q) ||
                        book.getId().toLowerCase(Locale.ROOT).contains(q)) {
                    result.add(book);
                }
            }
            return result;
        }

        // ---------- Reservation Logic ----------

        private void cleanupExpiredReservations() {
            Iterator<Reservation> it = reservations.iterator();
            while (it.hasNext()) {
                Reservation r = it.next();
                if (r.isExpired(RESERVATION_TTL)) {
                    it.remove();
                }
            }
        }

        public List<Reservation> getReservationsForBook(String bookId) {
            cleanupExpiredReservations();
            List<Reservation> result = new ArrayList<>();
            for (Reservation r : reservations) {
                if (r.getBookId().equals(bookId)) {
                    result.add(r);
                }
            }
            // Order by creation time (FIFO)
            result.sort(Comparator.comparing(Reservation::getCreatedAt));
            return result;
        }

        public List<Reservation> getReservationsForUser(String userId) {
            cleanupExpiredReservations();
            List<Reservation> result = new ArrayList<>();
            for (Reservation r : reservations) {
                if (r.getUserId().equals(userId)) {
                    result.add(r);
                }
            }
            result.sort(Comparator.comparing(Reservation::getCreatedAt));
            return result;
        }

        public Reservation createReservation(String userId, String bookId) {
            cleanupExpiredReservations();

            if (!books.containsKey(bookId)) {
                throw new IllegalArgumentException("Book does not exist.");
            }
            if (!users.containsKey(userId)) {
                throw new IllegalArgumentException("User does not exist.");
            }

            // Check if user already has an active reservation for this book
            for (Reservation r : reservations) {
                if (r.getUserId().equals(userId) && r.getBookId().equals(bookId) && !r.isExpired(RESERVATION_TTL)) {
                    throw new IllegalStateException("User already has an active reservation for this book.");
                }
            }

            Reservation reservation = new Reservation(userId, bookId);
            reservations.add(reservation);
            return reservation;
        }

        public boolean cancelReservation(long reservationId, String userId) {
            Iterator<Reservation> it = reservations.iterator();
            while (it.hasNext()) {
                Reservation r = it.next();
                if (r.getReservationId() == reservationId && r.getUserId().equals(userId)) {
                    it.remove();
                    return true;
                }
            }
            return false;
        }

        // ---------- Loan / Borrow / Return Logic ----------

        private int countActiveLoansForUser(String userId) {
            int count = 0;
            for (Loan loan : loans) {
                if (loan.getUserId().equals(userId) && loan.isActive()) {
                    count++;
                }
            }
            return count;
        }

        public List<Loan> getActiveLoansForUser(String userId) {
            List<Loan> result = new ArrayList<>();
            for (Loan loan : loans) {
                if (loan.getUserId().equals(userId) && loan.isActive()) {
                    result.add(loan);
                }
            }
            return result;
        }

        public List<Loan> getAllLoans() {
            return loans;
        }

        public Loan borrowBook(String userId, String bookId) {
            cleanupExpiredReservations();

            User user = users.get(userId);
            Book book = books.get(bookId);

            if (user == null) {
                throw new IllegalArgumentException("User not found.");
            }
            if (book == null) {
                throw new IllegalArgumentException("Book not found.");
            }

            if (countActiveLoansForUser(userId) >= MAX_BORROWED_PER_USER) {
                throw new IllegalStateException("User has reached the maximum number of borrowed books (" +
                        MAX_BORROWED_PER_USER + ").");
            }

            List<Reservation> bookReservations = getReservationsForBook(bookId);

            if (!bookReservations.isEmpty()) {
                // There are reservations; first reservation in queue has priority.
                Reservation first = bookReservations.get(0);
                if (!first.getUserId().equals(userId)) {
                    throw new IllegalStateException("Book is reserved by other users. You cannot borrow it now.");
                }
                // If first reservation belongs to this user and not expired, allow borrow and remove reservation
                reservations.remove(first);
            }

            if (book.getAvailableCopies() <= 0) {
                throw new IllegalStateException("No copies available to borrow.");
            }

            book.decreaseAvailable();
            Loan loan = new Loan(userId, bookId);
            loans.add(loan);
            return loan;
        }

        public Loan returnBook(long loanId) {
            for (Loan loan : loans) {
                if (loan.getLoanId() == loanId && loan.isActive()) {
                    Book book = books.get(loan.getBookId());
                    if (book != null) {
                        book.increaseAvailable();
                    }
                    loan.markReturned();
                    return loan;
                }
            }
            throw new IllegalArgumentException("Active loan not found with ID: " + loanId);
        }

        // ---------- Librarian Functions ----------

        public void updateBookCopies(String bookId, int newTotal) {
            Book book = books.get(bookId);
            if (book == null) {
                throw new IllegalArgumentException("Book not found.");
            }
            if (newTotal < 0) {
                throw new IllegalArgumentException("Total copies cannot be negative.");
            }
            book.setTotalCopies(newTotal);
        }

        public boolean removeBook(String bookId) {
            Book book = books.get(bookId);
            if (book == null) return false;

            // Prevent removal if there are active loans
            for (Loan loan : loans) {
                if (loan.getBookId().equals(bookId) && loan.isActive()) {
                    throw new IllegalStateException("Cannot remove book; there are active loans for this title.");
                }
            }

            // Remove reservations for this book
            reservations.removeIf(r -> r.getBookId().equals(bookId));
            books.remove(bookId);
            return true;
        }

        public void printReservationQueueForBook(String bookId) {
            List<Reservation> list = getReservationsForBook(bookId);
            if (list.isEmpty()) {
                System.out.println("No reservations for this book.");
                return;
            }
            System.out.println("Reservations for book " + bookId + " (FIFO):");
            for (Reservation r : list) {
                User u = users.get(r.getUserId());
                System.out.println("  ReservationId: " + r.getReservationId() +
                        " | User: " + (u != null ? u.getName() : r.getUserId()) +
                        " | CreatedAt: " + r.getCreatedAt());
            }
        }
    }

    // -------------------- Console UI --------------------

    private static final Scanner scanner = new Scanner(System.in);
    private static final Library library = new Library();
    private static User currentUser = null;

    public static void main(String[] args) {
        while (true) {
            if (currentUser == null) {
                showLoginMenu();
            } else {
                if (currentUser.isLibrarian()) {
                    showLibrarianMenu();
                } else {
                    showUserMenu();
                }
            }
        }
    }

    private static void showLoginMenu() {
        System.out.println("===== Library Reservation System =====");
        System.out.println("1. Login");
        System.out.println("2. List all users");
        System.out.println("0. Exit");
        System.out.print("Choose option: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                handleLogin();
                break;
            case "2":
                listAllUsers();
                break;
            case "0":
                System.out.println("Exiting...");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private static void handleLogin() {
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine().trim();
        User user = library.getUser(userId);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }
        if (user.isLibrarian()) {
            System.out.print("Enter Librarian Password (default 'admin'): ");
            String pwd = scanner.nextLine().trim();
            if (!"admin".equals(pwd)) {
                System.out.println("Invalid password.");
                return;
            }
        }
        currentUser = user;
        System.out.println("Logged in as: " + currentUser.getName() +
                (currentUser.isLibrarian() ? " (Librarian)" : " (Student/Staff)"));
    }

    private static void listAllUsers() {
        System.out.println("Registered users:");
        for (User u : library.getAllUsers()) {
            System.out.println("  ID: " + u.getId() +
                    " | Name: " + u.getName() +
                    " | Role: " + (u.isLibrarian() ? "Librarian" : "Student/Staff"));
        }
    }

    // ---------- Student/Staff Menu ----------

    private static void showUserMenu() {
        System.out.println("\n===== User Menu (" + currentUser.getName() + ") =====");
        System.out.println("1. Search books");
        System.out.println("2. View all books");
        System.out.println("3. Reserve a book");
        System.out.println("4. View my reservations");
        System.out.println("5. Cancel a reservation");
        System.out.println("6. Borrow a book");
        System.out.println("7. View my borrowed books");
        System.out.println("8. Return a book");
        System.out.println("9. Logout");
        System.out.print("Choose option: ");
        String choice = scanner.nextLine().trim();

        try {
            switch (choice) {
                case "1":
                    handleSearchBooks();
                    break;
                case "2":
                    handleViewAllBooks();
                    break;
                case "3":
                    handleReserveBook();
                    break;
                case "4":
                    handleViewMyReservations();
                    break;
                case "5":
                    handleCancelReservation();
                    break;
                case "6":
                    handleBorrowBook();
                    break;
                case "7":
                    handleViewMyLoans();
                    break;
                case "8":
                    handleReturnBook();
                    break;
                case "9":
                    currentUser = null;
                    System.out.println("Logged out.");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } catch (Exception ex) {
            System.out.println("Operation failed: " + ex.getMessage());
        }
    }

    // ---------- Librarian Menu ----------

    private static void showLibrarianMenu() {
        System.out.println("\n===== Librarian Menu (" + currentUser.getName() + ") =====");
        System.out.println("1. Search books");
        System.out.println("2. View all books");
        System.out.println("3. Add new book");
        System.out.println("4. Update book copies");
        System.out.println("5. Remove book");
        System.out.println("6. View all loans");
        System.out.println("7. View reservation queue for a book");
        System.out.println("8. Logout");
        System.out.print("Choose option: ");
        String choice = scanner.nextLine().trim();

        try {
            switch (choice) {
                case "1":
                    handleSearchBooks();
                    break;
                case "2":
                    handleViewAllBooks();
                    break;
                case "3":
                    handleAddBook();
                    break;
                case "4":
                    handleUpdateBookCopies();
                    break;
                case "5":
                    handleRemoveBook();
                    break;
                case "6":
                    handleViewAllLoans();
                    break;
                case "7":
                    handleViewReservationQueueForBook();
                    break;
                case "8":
                    currentUser = null;
                    System.out.println("Logged out.");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } catch (Exception ex) {
            System.out.println("Operation failed: " + ex.getMessage());
        }
    }

    // ---------- Handlers (Shared) ----------

    private static void handleSearchBooks() {
        System.out.print("Enter search query (title/author/id): ");
        String q = scanner.nextLine().trim();
        List<Book> results = library.searchBooks(q);
        if (results.isEmpty()) {
            System.out.println("No books found.");
        } else {
            System.out.println("Found books:");
            for (Book b : results) {
                System.out.println("  " + b);
            }
        }
    }

    private static void handleViewAllBooks() {
        System.out.println("All books in catalogue:");
        for (Book b : library.getAllBooks()) {
            System.out.println("  " + b);
        }
    }

    // ---------- User handlers ----------

    private static void handleReserveBook() {
        System.out.print("Enter Book ID to reserve: ");
        String bookId = scanner.nextLine().trim();
        Reservation r = library.createReservation(currentUser.getId(), bookId);
        System.out.println("Reservation created: " + r);
    }

    private static void handleViewMyReservations() {
        List<Reservation> list = library.getReservationsForUser(currentUser.getId());
        if (list.isEmpty()) {
            System.out.println("You have no reservations.");
            return;
        }
        System.out.println("Your reservations:");
        for (Reservation r : list) {
            Book b = library.getBook(r.getBookId());
            System.out.println("  ReservationId: " + r.getReservationId() +
                    " | Book: " + (b != null ? b.getTitle() : r.getBookId()) +
                    " | CreatedAt: " + r.getCreatedAt());
        }
    }

    private static void handleCancelReservation() {
        System.out.print("Enter Reservation ID to cancel: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        boolean ok = library.cancelReservation(id, currentUser.getId());
        if (ok) {
            System.out.println("Reservation canceled.");
        } else {
            System.out.println("Reservation not found or not owned by you.");
        }
    }

    private static void handleBorrowBook() {
        System.out.print("Enter Book ID to borrow: ");
        String bookId = scanner.nextLine().trim();
        Loan loan = library.borrowBook(currentUser.getId(), bookId);
        System.out.println("Book borrowed successfully. Loan: " + loan);
    }

    private static void handleViewMyLoans() {
        List<Loan> list = library.getActiveLoansForUser(currentUser.getId());
        if (list.isEmpty()) {
            System.out.println("You have no active loans.");
            return;
        }
        System.out.println("Your active loans:");
        for (Loan loan : list) {
            Book b = library.getBook(loan.getBookId());
            System.out.println("  LoanId: " + loan.getLoanId() +
                    " | Book: " + (b != null ? b.getTitle() : loan.getBookId()) +
                    " | BorrowedAt: " + loan.getBorrowedAt());
        }
    }

    private static void handleReturnBook() {
        System.out.print("Enter Loan ID to return: ");
        long loanId = Long.parseLong(scanner.nextLine().trim());
        Loan loan = library.returnBook(loanId);
        System.out.println("Book returned. Updated loan: " + loan);
    }

    // ---------- Librarian handlers ----------

    private static void handleAddBook() {
        System.out.print("Enter new Book ID: ");
        String id = scanner.nextLine().trim();
        if (library.getBook(id) != null) {
            System.out.println("Book ID already exists.");
            return;
        }
        System.out.print("Enter title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Enter author: ");
        String author = scanner.nextLine().trim();
        System.out.print("Enter total copies: ");
        int copies = Integer.parseInt(scanner.nextLine().trim());

        library.addBook(new Book(id, title, author, copies));
        System.out.println("Book added.");
    }

    private static void handleUpdateBookCopies() {
        System.out.print("Enter Book ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Enter new total copies: ");
        int copies = Integer.parseInt(scanner.nextLine().trim());
        library.updateBookCopies(id, copies);
        System.out.println("Book copies updated.");
    }

    private static void handleRemoveBook() {
        System.out.print("Enter Book ID to remove: ");
        String id = scanner.nextLine().trim();
        boolean ok = library.removeBook(id);
        if (ok) {
            System.out.println("Book removed.");
        } else {
            System.out.println("Book not found.");
        }
    }

    private static void handleViewAllLoans() {
        List<Loan> all = library.getAllLoans();
        if (all.isEmpty()) {
            System.out.println("No loans recorded yet.");
            return;
        }
        System.out.println("All loans (active and returned):");
        for (Loan loan : all) {
            Book b = library.getBook(loan.getBookId());
            User u = library.getUser(loan.getUserId());
            System.out.println("  LoanId: " + loan.getLoanId() +
                    " | User: " + (u != null ? u.getName() : loan.getUserId()) +
                    " | Book: " + (b != null ? b.getTitle() : loan.getBookId()) +
                    " | BorrowedAt: " + loan.getBorrowedAt() +
                    " | ReturnedAt: " + loan.getReturnedAt());
        }
    }

    private static void handleViewReservationQueueForBook() {
        System.out.print("Enter Book ID: ");
        String id = scanner.nextLine().trim();
        library.printReservationQueueForBook(id);
    }
}
