package UAS.Tampilan; // Mendefinisikan paket tempat kelas ini berada

import javax.swing.*; // Mengimpor kelas-kelas Swing untuk GUI
import javax.swing.border.EmptyBorder; // Mengimpor kelas EmptyBorder untuk padding
import UAS.databaseParkir.dbParkir; // Mengimpor kelas dbParkir untuk koneksi database

import java.awt.*; // Mengimpor kelas-kelas AWT untuk grafis dan UI dasar
import java.awt.event.FocusAdapter; // Mengimpor FocusAdapter untuk menangani event fokus
import java.awt.event.FocusEvent; // Mengimpor FocusEvent untuk event fokus
import java.sql.Connection; // Mengimpor Connection untuk koneksi SQL
import java.sql.PreparedStatement; // Mengimpor PreparedStatement untuk query SQL yang aman
import java.sql.ResultSet; // Mengimpor ResultSet untuk menampung hasil query SQL

/**
 * Kelas loginAdmin merupakan JFrame yang berfungsi sebagai antarmuka login
 * untuk administrator sistem parkir.
 */
public class loginAdmin extends JFrame {

    // === KONSTANTA UNTUK STYLING ===
    // Warna utama yang digunakan dalam desain
    private static final Color PRIMARY = new Color(0, 0, 102); // Biru tua
    // Warna teks standar
    private static final Color TEXT = new Color(44, 62, 80); // Abu-abu tua
    // Font untuk judul utama
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    // Font untuk label standar
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 14);
    // Font untuk teks pada tombol
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);

    // === KOMPONEN UI ===
    // Field untuk input username
    private JTextField tfUsername;
    // Field untuk input password (disamarkan)
    private JPasswordField pfPassword;
    // Label untuk menampilkan status login (berhasil, gagal, error)
    private JLabel statusLabel;

    /**
     * Konstruktor kelas loginAdmin.
     * Menginisialisasi dan mengatur tampilan jendela login.
     */
    public loginAdmin() {
        setTitle("Admin Login - Sistem Parkir"); // Mengatur judul jendela
        setSize(800, 500); // Mengatur ukuran jendela (lebar, tinggi)
        setLocationRelativeTo(null); // Menampilkan jendela di tengah layar
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Mengatur aksi saat tombol close jendela diklik (menghentikan aplikasi)
        setResizable(false); // Mencegah ukuran jendela diubah oleh pengguna

        // Membuat panel utama dengan latar belakang gradien
        JPanel main = new JPanel(new BorderLayout()) {
            // Override paintComponent untuk menggambar latar belakang gradien
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                // Membuat gradien dari warna biru muda ke ungu muda
                g2.setPaint(new GradientPaint(0, 0, new Color(74, 144, 226), 0, getHeight(), new Color(143, 148, 251)));
                g2.fillRect(0, 0, getWidth(), getHeight()); // Mengisi seluruh panel dengan gradien
            }
        };

        // Menambahkan panel gambar di sisi kiri
        main.add(createImagePanel(), BorderLayout.WEST);
        // Menambahkan panel form login di sisi tengah (kanan dari gambar)
        main.add(createLoginPanel(), BorderLayout.CENTER);
        // Mengatur panel utama sebagai content pane dari JFrame
        setContentPane(main);
        // setVisible(true); // Komentar: Visibilitas diatur oleh pemanggil (misalnya, metode main)
    }

    /**
     * Membuat dan mengembalikan panel yang menampilkan logo dan judul aplikasi.
     */
    private JPanel createImagePanel() {
        // Panel untuk sisi kiri dengan GridBagLayout untuk penataan vertikal
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false); // Membuat panel transparan agar gradien di belakangnya terlihat
        panel.setPreferredSize(new Dimension(400, 500)); // Mengatur ukuran preferensi panel

        // Pengaturan untuk GridBagConstraints
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 0, 10, 0); // Padding antar komponen
        c.gridx = 0; // Semua komponen dalam satu kolom
        c.gridy = GridBagConstraints.RELATIVE; // Komponen ditumpuk secara vertikal
        c.anchor = GridBagConstraints.CENTER; // Penjajaran komponen di tengah

        // Mencoba memuat gambar logo
        ImageIcon iconImage = null;
        try {
            // Pastikan file "logoParkirin.png" ada di direktori yang sama dengan kelas loginAdmin.class
            // atau di path yang dapat diakses oleh getResource
            java.net.URL imgURL = getClass().getResource("logoParkirin.png");
            if (imgURL != null) { // Jika logo ditemukan
                iconImage = new ImageIcon(imgURL);
                // Mengubah ukuran logo menjadi 120x120 piksel dengan scaling yang halus
                Image scaledImage = iconImage.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                JLabel icon = new JLabel(new ImageIcon(scaledImage));
                panel.add(icon, c); // Menambahkan logo ke panel
            } else { // Jika logo tidak ditemukan
                JLabel noLogoLabel = new JLabel("Logo not found");
                noLogoLabel.setForeground(Color.WHITE);
                panel.add(noLogoLabel,c);
                System.err.println("Resource logo tidak ditemukan: logoParkirin.png");
            }
        } catch (Exception e) { // Menangani error jika gagal memuat logo
            JLabel errorLogoLabel = new JLabel("Error loading logo");
            errorLogoLabel.setForeground(Color.WHITE);
            panel.add(errorLogoLabel,c);
            System.err.println("Error saat memuat logo: " + e.getMessage());
            e.printStackTrace();
        }

        // Label judul utama aplikasi "PARKIRIN"
        JLabel title = new JLabel("PARKIRIN", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28)); // Mengatur font
        title.setForeground(Color.WHITE); // Mengatur warna teks
        panel.add(title, c); // Menambahkan judul ke panel

        // Label subjudul "Sistem Manajemen Parkir"
        JLabel subtitle = new JLabel("Sistem Manajemen Parkir", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Mengatur font
        subtitle.setForeground(new Color(255, 255, 255, 180)); // Mengatur warna teks (putih semi-transparan)
        panel.add(subtitle, c); // Menambahkan subjudul ke panel

        return panel; // Mengembalikan panel yang sudah jadi
    }

    /**
     * Membuat dan mengembalikan panel yang berisi form login (username, password, tombol login).
     */
    private JPanel createLoginPanel() {
        // Panel untuk form login dengan GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE); // Latar belakang panel putih
        panel.setBorder(new EmptyBorder(40, 40, 40, 40)); // Padding di sekeliling panel

        // Pengaturan untuk GridBagConstraints
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 0, 10, 0); // Padding antar komponen
        c.fill = GridBagConstraints.HORIZONTAL; // Komponen mengisi ruang secara horizontal

        // Label judul "Login Admin"
        JLabel title = new JLabel("Login Admin", SwingConstants.CENTER);
        title.setFont(FONT_TITLE); // Mengatur font dari konstanta
        title.setForeground(TEXT); // Mengatur warna teks dari konstanta
        c.gridwidth = 2; // Label ini memanjang selebar 2 kolom
        c.gridy = 0; // Posisi baris ke-0
        panel.add(title, c);

        // Mengembalikan gridwidth ke 1 untuk komponen berikutnya
        c.gridwidth = 1;

        // Label dan field untuk Username
        c.gridy = 1; // Baris ke-1
        c.gridx = 0; // Kolom ke-0 (untuk label)
        c.anchor = GridBagConstraints.WEST; // Label rata kiri
        panel.add(new JLabel("Username:", JLabel.LEFT), c);

        c.gridy = 2; // Baris ke-2
        c.gridx = 0; // Kolom ke-0
        c.gridwidth = 2; // Field username memanjang 2 kolom
        tfUsername = (JTextField) createStyledField(false); // Membuat field dengan style khusus
        panel.add(tfUsername, c);


        // Label dan field untuk Password
        c.gridy = 3; // Baris ke-3
        c.gridx = 0; // Kolom ke-0 (untuk label)
        c.gridwidth = 1; // Label hanya 1 kolom
        c.anchor = GridBagConstraints.WEST; // Label rata kiri
        panel.add(new JLabel("Password:", JLabel.LEFT), c);

        c.gridy = 4; // Baris ke-4
        c.gridx = 0; // Kolom ke-0
        c.gridwidth = 2; // Field password memanjang 2 kolom
        pfPassword = (JPasswordField) createStyledField(true); // Membuat field password dengan style khusus
        panel.add(pfPassword, c);


        // Tombol Login
        JButton login = createStyledButton("LOGIN"); // Membuat tombol dengan style khusus
        // Mengatur agar tombol login ini menjadi tombol default (bisa ditekan dengan Enter)
        getRootPane().setDefaultButton(login);

        // Menambahkan ActionListener untuk menangani event klik pada tombol login
        login.addActionListener(_ -> { // Menggunakan lambda expression untuk ActionListener
            String username = tfUsername.getText(); // Mendapatkan teks dari field username
            String password = new String(pfPassword.getPassword()); // Mendapatkan teks dari field password

            // Blok try-with-resources untuk koneksi database (otomatis menutup koneksi)
            try (Connection conn = dbParkir.getConnection()) {
                // Query SQL untuk memeriksa username dan password di tabel admin
                String sql = "SELECT * FROM admin WHERE username = ? AND password = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username); // Mengatur parameter pertama (username)
                stmt.setString(2, password); // Mengatur parameter kedua (password)
                ResultSet rs = stmt.executeQuery(); // Menjalankan query dan mendapatkan hasilnya

                if (rs.next()) { // Jika ditemukan data yang cocok (login berhasil)
                    JOptionPane.showMessageDialog(this, "Login berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // Menutup jendela login saat ini

                    // Menjalankan pembuatan jendela MainApplication di Event Dispatch Thread (EDT) Swing
                    SwingUtilities.invokeLater(() -> {
                        MainApplication mainApp = new MainApplication(); // Membuat instance MainApplication
                        mainApp.setVisible(true); // Menampilkan MainApplication
                        // Komentar: Konstruktor MainApplication mungkin sudah mengatur setVisible(true)
                    });

                } else { // Jika tidak ada data yang cocok (login gagal)
                    statusLabel.setForeground(Color.RED); // Warna teks status menjadi merah
                    statusLabel.setText("Username atau password salah."); // Menampilkan pesan kesalahan
                }
            } catch (Exception ex) { // Menangani error umum (termasuk SQLException)
                ex.printStackTrace(); // Mencetak stack trace error ke konsol
                // Menampilkan dialog error ke pengguna
                JOptionPane.showMessageDialog(this, "Terjadi kesalahan koneksi database.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        c.gridy = 5; // Baris ke-5
        c.gridx = 0; // Kolom ke-0
        c.gridwidth = 2; // Tombol memanjang 2 kolom
        c.anchor = GridBagConstraints.CENTER; // Tombol di tengah
        panel.add(login, c); // Menambahkan tombol login ke panel

        // Label untuk menampilkan status login
        statusLabel = new JLabel(" ", SwingConstants.CENTER); // Teks awal kosong, rata tengah
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Mengatur font
        c.gridy = 6; // Baris ke-6
        c.gridx = 0; // Kolom ke-0
        c.gridwidth = 2; // Label status memanjang 2 kolom
        panel.add(statusLabel, c); // Menambahkan label status ke panel

        return panel; // Mengembalikan panel form login yang sudah jadi
    }

    /**
     * Membuat dan mengembalikan JTextField atau JPasswordField dengan style tertentu.
     */
    private JTextField createStyledField(boolean isPassword) {
        // Membuat field, JPasswordField jika isPassword true, jika tidak maka JTextField
        JTextField field = isPassword ? new JPasswordField(20) : new JTextField(20);
        field.setFont(FONT_LABEL); // Mengatur font dari konstanta
        // Mengatur border dengan kombinasi LineBorder dan EmptyBorder (untuk padding dalam)
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1), // Border luar abu-abu muda
                BorderFactory.createEmptyBorder(8, 10, 8, 10))); // Padding dalam (atas, kiri, bawah, kanan)

        // Menambahkan FocusListener untuk mengubah tampilan border saat field mendapat fokus
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) { // Saat field mendapat fokus
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY, 2), // Border menjadi warna PRIMARY, tebal 2 piksel
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            }

            @Override
            public void focusLost(FocusEvent e) { // Saat field kehilangan fokus
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199), 1), // Border kembali normal
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            }
        });
        return field; // Mengembalikan field yang sudah diberi style
    }

    /**
     * Membuat dan mengembalikan JButton dengan style tertentu.
     */
    private JButton createStyledButton(String text) {
        JButton b = new JButton(text); // Membuat tombol dengan teks yang diberikan
        b.setFont(FONT_BUTTON); // Mengatur font dari konstanta
        b.setForeground(Color.WHITE); // Warna teks putih
        b.setBackground(PRIMARY); // Warna latar belakang PRIMARY (biru tua)
        b.setFocusPainted(false); // Menghilangkan efek visual fokus standar
        b.setContentAreaFilled(true); // Memastikan area tombol diisi dengan warna latar belakang
        b.setBorderPainted(false); // Menghilangkan border standar tombol
        b.setOpaque(true); // Diperlukan agar setBackground berfungsi di beberapa Look & Feel
        b.setPreferredSize(new Dimension(150, 40)); // Mengatur ukuran preferensi tombol
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Mengubah kursor menjadi tangan saat mouse di atas tombol
        b.setHorizontalAlignment(SwingConstants.CENTER); // Penjajaran teks default untuk JButton sudah tengah

        // Efek hover sederhana (warna tombol menjadi lebih gelap saat mouse masuk)
        Color primaryDarker = PRIMARY.darker(); // Warna PRIMARY yang sedikit lebih gelap
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { // Saat mouse masuk area tombol
                b.setBackground(primaryDarker);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) { // Saat mouse keluar area tombol
                b.setBackground(PRIMARY); // Warna kembali normal
            }
        });
        return b; // Mengembalikan tombol yang sudah diberi style
    }

    /**
     * Metode main, titik masuk utama untuk menjalankan aplikasi login.
     */
    public static void main(String[] args) {
        // Mengatur Look and Feel aplikasi menjadi Nimbus jika tersedia, jika tidak menggunakan default sistem
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Jika Nimbus tidak tersedia, coba gunakan Look & Feel sistem
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace(); // Cetak error jika pengaturan Look & Feel gagal total
            }
        }
        // Menjalankan pembuatan dan penampilan GUI di Event Dispatch Thread (EDT) Swing
        // Ini adalah praktik terbaik untuk aplikasi Swing agar aman dari masalah thread
        SwingUtilities.invokeLater(() -> {
            loginAdmin loginFrame = new loginAdmin(); // Membuat instance jendela login
            loginFrame.setVisible(true); // Menampilkan jendela login
        });
    }
}