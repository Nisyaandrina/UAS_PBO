package UAS.Tampilan;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Kelas MainApplication berfungsi sebagai jendela utama aplikasi sistem parkir.
 * Menampilkan berbagai panel fungsionalitas dalam bentuk tab.
 */
public class MainApplication extends JFrame {

    // --- Definisi Variabel Warna untuk UI ---
    // Variabel-variabel ini digunakan untuk konsistensi tampilan di seluruh aplikasi.
    private Color colorBackground = new Color(230, 240, 250);
    private Color colorHeaderPanel = new Color(70, 130, 180); // Warna untuk panel header
    private Color colorHeaderText = Color.WHITE;
    private Color colorTabBackground = new Color(230, 240, 250); // Warna background tab
    private Color colorTabForeground = new Color(70, 130, 180); // Warna teks tab
    private Color colorLogoutButton = new Color(220, 20, 60); // Merah (Crimson)
    private Color colorLogoutButtonHover = new Color(139, 0, 0); // Merah gelap untuk hover
    private Color colorButtonText = Color.WHITE;

    // Variabel instance untuk PanelRiwayatParkir agar bisa diakses dan di-refresh
    private PanelRiwayatParkir panelRiwayat;

    /**
     * Konstruktor utama untuk MainApplication.
     * Menginisialisasi semua komponen UI dan menampilkannya.
     */
    public MainApplication() {
        setTitle("Sistem Parkir Professional - Parkirin"); // Judul jendela aplikasi
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Aksi saat tombol close diklik (menutup aplikasi)
        setSize(750, 700); // Mengatur ukuran default jendela
        setLocationRelativeTo(null); // Menampilkan jendela di tengah layar
        getContentPane().setBackground(colorBackground); // Mengatur warna latar belakang utama jendela

        // Panel utama yang menampung semua komponen lain
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)); // Menggunakan BorderLayout dengan jarak 10px
        mainPanel.setBackground(colorBackground);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Memberi padding di sekeliling mainPanel

        // --- Pembuatan Panel Header ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10)); // FlowLayout rata kiri dengan spasi
        headerPanel.setBackground(colorHeaderPanel);
        headerPanel.setPreferredSize(new Dimension(getWidth(), 70)); // Mengatur tinggi preferensi header

        // --- Logika Pemuatan Logo ---
        try {
            java.net.URL logoURL = getClass().getResource("logoParkirin.png"); // Mencari resource logo
            if (logoURL != null) { // Jika logo ditemukan
                ImageIcon originalLogo = new ImageIcon(logoURL);
                // Mengubah ukuran logo agar sesuai dengan header
                Image scaledLogo = originalLogo.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
                headerPanel.add(logoLabel); // Menambahkan logo ke header
            } else {
                System.err.println("Logo 'logoParkirin.png' tidak ditemukan di UAS/Tampilan/");
            }
        } catch (Exception e) { // Menangani jika ada error saat memuat logo
            System.err.println("Error saat memuat logo: " + e.getMessage());
            e.printStackTrace();
        }

        // Label judul utama di header
        JLabel titleLabel = new JLabel("Sistem Manajemen Parkir - Parkirin");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(colorHeaderText);
        headerPanel.add(titleLabel); // Menambahkan judul ke header
        mainPanel.add(headerPanel, BorderLayout.NORTH); // Menempatkan header di bagian atas mainPanel

        // --- Pembuatan JTabbedPane untuk Navigasi antar Panel ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14)); // Font untuk judul tab
        tabbedPane.setBackground(colorTabBackground); // Warna latar belakang area tab
        tabbedPane.setForeground(colorTabForeground); // Warna teks untuk judul tab

        // --- Menambahkan Panel-Panel sebagai Tab ---
        // Tab untuk Proses Parkir Masuk
        PanelParkirMasuk panelMasuk = new PanelParkirMasuk();
        tabbedPane.addTab(" Proses Parkir Masuk ", null, panelMasuk, "Formulir untuk mencatat kendaraan masuk");

        // Tab untuk Proses Parkir Keluar
        PanelParkirKeluar panelKeluar = new PanelParkirKeluar();
        tabbedPane.addTab(" Proses Parkir Keluar ", null, panelKeluar, "Formulir untuk memproses kendaraan keluar");

        // Tab untuk Riwayat Data Parkir
        panelRiwayat = new PanelRiwayatParkir(); // Inisialisasi panelRiwayat sebagai variabel instance
        tabbedPane.addTab(" Riwayat Data Parkir ", null, panelRiwayat, "Melihat riwayat data kendaraan");

        // Tab untuk Logout
        JPanel panelLogout = createLogoutPanel(); // Memanggil metode untuk membuat panel logout
        tabbedPane.addTab(" Logout ", null, panelLogout, "Keluar dari Program Parkir");
        
        // --- Listener untuk JTabbedPane ---
        // Listener ini akan aktif ketika pengguna mengganti tab.
        // Berguna untuk me-refresh data pada PanelRiwayatParkir saat tab tersebut dipilih.
        tabbedPane.addChangeListener(_ -> { // Menggunakan lambda expression
            if (tabbedPane.getSelectedComponent() == panelRiwayat) {
                panelRiwayat.refreshData(); // Memanggil metode refreshData di PanelRiwayatParkir
            }
        });


        mainPanel.add(tabbedPane, BorderLayout.CENTER); // Menempatkan tabbedPane di tengah mainPanel
        add(mainPanel); // Menambahkan mainPanel ke JFrame
        setVisible(true); // Menampilkan JFrame ke layar
    }

    /**
     * Membuat dan mengembalikan JPanel yang berisi fungsionalitas logout.
     * @return JPanel untuk proses logout.
     */
    private JPanel createLogoutPanel() {
        // Panel dasar untuk logout, menggunakan GridBagLayout untuk penataan komponen
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE); // Latar belakang panel logout
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(colorHeaderPanel, 2, true), // Border panel
            new EmptyBorder(20, 20, 20, 20) // Padding dalam panel
        ));

        GridBagConstraints gbc = new GridBagConstraints(); // Objek untuk mengatur posisi dan ukuran komponen
        gbc.gridx = 0; // Posisi kolom
        gbc.gridy = 0; // Posisi baris
        gbc.insets = new Insets(10, 10, 10, 10); // Jarak antar komponen
        gbc.anchor = GridBagConstraints.CENTER; // Penjajaran komponen di tengah

        // Label konfirmasi logout
        JLabel lblConfirm = new JLabel("Apakah Anda yakin ingin keluar dari sistem?");
        lblConfirm.setFont(new Font("Arial", Font.PLAIN, 16));
        lblConfirm.setForeground(new Color(50, 50, 50));
        panel.add(lblConfirm, gbc);

        gbc.gridy++; // Pindah ke baris berikutnya untuk tombol logout
        JButton logoutButton = new JButton("Logout Sekarang");
        // Styling tombol logout
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.setBackground(colorLogoutButton);
        logoutButton.setForeground(colorButtonText);
        logoutButton.setFocusPainted(false); // Tidak menampilkan border fokus
        logoutButton.setBorder(new EmptyBorder(10, 20, 10, 20)); // Padding tombol
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Kursor tangan saat mouse di atas tombol
        logoutButton.setPreferredSize(new Dimension(200, 45)); // Ukuran preferensi tombol

        // Efek hover untuk tombol logout
        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(colorLogoutButtonHover); // Warna berubah saat mouse masuk
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(colorLogoutButton); // Warna kembali normal saat mouse keluar
            }
        });

        // Aksi ketika tombol logout diklik
        logoutButton.addActionListener(_ -> {
            // Menampilkan dialog konfirmasi sebelum logout
            int response = JOptionPane.showConfirmDialog(
                this, // Komponen parent untuk dialog
                "Anda akan keluar dari aplikasi. Lanjutkan?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (response == JOptionPane.YES_OPTION) { // Jika pengguna memilih "Yes"
                dispose(); // Menutup jendela MainApplication saat ini
                // Kembali ke jendela login
                SwingUtilities.invokeLater(() -> { // Menjalankan di Event Dispatch Thread
                    loginAdmin loginFrame = new loginAdmin();
                    loginFrame.setVisible(true); // Menampilkan kembali jendela login
                });
            }
        });
        panel.add(logoutButton, gbc); // Menambahkan tombol logout ke panel
        
        gbc.gridy++; // Pindah ke baris berikutnya
        gbc.weighty = 1.0; // Memberikan sisa ruang vertikal ke komponen dummy di bawah ini
        panel.add(new JLabel(), gbc); // Komponen dummy untuk mendorong tombol ke tengah-atas

        return panel; // Mengembalikan panel logout yang sudah jadi
    }
}