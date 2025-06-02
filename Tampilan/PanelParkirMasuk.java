package UAS.Tampilan; // Pastikan paketnya sesuai dengan struktur proyek Anda

import javax.swing.*; // Mengimpor kelas-kelas Swing untuk GUI
import javax.swing.border.EmptyBorder; // Mengimpor kelas EmptyBorder untuk padding
import javax.swing.border.LineBorder; // Mengimpor kelas LineBorder untuk garis tepi
import java.awt.*; // Mengimpor kelas-kelas AWT untuk grafis dan UI dasar
import java.sql.Connection; // Mengimpor Connection untuk koneksi SQL
import java.sql.PreparedStatement; // Mengimpor PreparedStatement untuk query SQL yang aman
import java.sql.ResultSet; // Ditambahkan untuk menangani hasil query dari checkStmt (sebelumnya menggunakan var)
import java.sql.SQLException; // Mengimpor SQLException untuk menangani error SQL
import java.sql.Statement; // Import Statement (sudah ada dan benar)

// Mengimpor kelas-kelas yang dibutuhkan dari paket lain
import UAS.databaseParkir.dbParkir; // Untuk koneksi ke database
// import UAS.Tampilan.DataUpdateListener; // Komentar ini sudah baik, mengingatkan dependensi

/**
 * Kelas PanelParkirMasuk adalah JPanel yang menangani semua fungsionalitas
 * terkait proses pencatatan kendaraan yang masuk ke area parkir.
 * Termasuk validasi input, pembuatan ID tiket, penyimpanan ke database,
 * dan pemberitahuan jika ada perubahan data.
 */
public class PanelParkirMasuk extends JPanel {

    // === KOMPONEN UI ===
    // Field untuk ID tiket (otomatis dibuat, read-only), plat nomor, dan jam masuk
    private JTextField tfIdTiketMasuk, tfPlatNomorMasuk, tfJamMasuk;
    // Radio button untuk memilih jenis kendaraan
    private JRadioButton rbMobilMasuk, rbMobilBoxMasuk, rbMotorMasuk;
    // Grup untuk radio button jenis kendaraan agar hanya satu yang bisa dipilih
    private ButtonGroup bgJenisKendaraanMasuk;
    // Tombol aksi: simpan data parkir masuk dan hapus isi form
    private JButton simpanButtonMasuk, hapusButtonMasuk;

    // Counter untuk generate ID tiket. Tidak lagi diinisialisasi ke 0 di sini.
    // Diinisialisasi dari database melalui metode statis.
    private static int nomorTiketCounter;

    // Variabel untuk listener pembaruan data,
    // digunakan untuk memberitahu komponen lain (misal PanelRiwayatParkir) jika ada data baru.
    private DataUpdateListener dataUpdateListener;

    // === DEFINISI WARNA UMUM UNTUK UI ===
    // Variabel warna ini membantu menjaga konsistensi visual di seluruh panel.
    private Color colorPanel = new Color(255, 255, 255);
    private Color colorHeaderBorder = new Color(70, 130, 180);
    private Color colorButton = new Color(70, 130, 180);
    private Color colorButtonText = Color.WHITE;
    private Color colorInputBorder = new Color(173, 216, 230);
    private Color colorLabelText = new Color(50, 50, 50);
    private Color colorReadOnlyBg = new Color(235,235,235);
    private Color colorReadOnlyText = new Color(80,80,80);

    // Blok static ini dieksekusi hanya sekali ketika kelas PanelParkirMasuk pertama kali dimuat.
    // Tujuannya adalah untuk menginisialisasi nomorTiketCounter dari database.
    static {
        initializeNomorTiketCounterFromDB();
    }

    /**
     * Menginisialisasi nomorTiketCounter dengan mengambil ID tiket terakhir
     * dari database untuk memastikan kelanjutan nomor tiket setelah aplikasi dimulai ulang.
     */
    private static void initializeNomorTiketCounterFromDB() {
        String lastIdTiket = null;
        // Menggunakan try-with-resources untuk manajemen otomatis sumber daya (koneksi, statement, resultset).
        try (Connection conn = dbParkir.getConnection();
             Statement stmt = conn.createStatement();
             // Query untuk mendapatkan id_tiket dengan format PKXXX yang paling besar secara leksikografis.
             // ORDER BY id_tiket DESC mengambil urutan dari besar ke kecil, LIMIT 1 mengambil baris pertama (terbesar).
             ResultSet rs = stmt.executeQuery("SELECT id_tiket FROM parkiran WHERE id_tiket LIKE 'PK%' ORDER BY id_tiket DESC LIMIT 1")) {

            if (rs.next()) { // Jika query mengembalikan hasil (ada data tiket sebelumnya)
                lastIdTiket = rs.getString("id_tiket");
            }
        } catch (SQLException e) { // Menangani error jika koneksi atau query database gagal
            System.err.println("Error saat mengambil ID tiket terakhir dari DB: " + e.getMessage());
            e.printStackTrace();
            // Jika terjadi error, nomorTiketCounter akan tetap pada nilai defaultnya (0 setelah parsing gagal atau tidak ada data).
            // Aplikasi mungkin masih bisa berjalan, namun ID tiket bisa jadi mengulang jika DB error.
        }

        if (lastIdTiket != null && lastIdTiket.matches("PK\\d+")) { // Memastikan formatnya "PK" diikuti satu atau lebih digit.
            try {
                // Ekstrak bagian numerik dari ID tiket terakhir (misal, dari "PK005" menjadi 5).
                // substring(2) mengambil karakter mulai dari indeks ke-2 (setelah "PK").
                nomorTiketCounter = Integer.parseInt(lastIdTiket.substring(2));
            } catch (NumberFormatException e) { // Menangani error jika bagian numerik tidak bisa di-parse menjadi integer.
                System.err.println("Error parsing nomor dari ID tiket terakhir: " + lastIdTiket + " - " + e.getMessage());
                nomorTiketCounter = 0; // Jika gagal parsing, default ke 0 untuk menghindari error lebih lanjut.
            }
        } else {
            // Jika tidak ada tiket sebelumnya di database atau formatnya tidak valid, mulai counter dari 0.
            nomorTiketCounter = 0;
        }
        // Pesan debugging untuk memverifikasi nilai inisialisasi counter. Bisa dihapus di versi production.
        System.out.println("Sistem Parkir: Nomor tiket counter diinisialisasi ke: " + nomorTiketCounter);
    }

    /**
     * Konstruktor PanelParkirMasuk.
     * Memanggil metode initComponents untuk membangun dan menata antarmuka pengguna.
     */
    public PanelParkirMasuk() {
        initComponents();
    }

    /**
     * Metode publik untuk mengatur (mengeset) listener pembaruan data.
     * Memungkinkan objek lain (seperti MainApplication) untuk menerima notifikasi
     * ketika data di panel ini berubah (misalnya, tiket baru disimpan).
     * @param listener Objek DataUpdateListener yang akan diberitahu.
     */
    public void setDataUpdateListener(DataUpdateListener listener) {
        this.dataUpdateListener = listener;
    }

    /**
     * Menginisialisasi dan menata semua komponen antarmuka pengguna (UI) pada panel ini.
     * Menggunakan GridBagLayout untuk penataan komponen yang fleksibel.
     */
    private void initComponents() {
        setBackground(colorPanel); // Mengatur warna latar belakang panel.
        // Mengatur border panel dengan kombinasi LineBorder (garis) dan EmptyBorder (padding).
        setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(colorHeaderBorder, 2, true), // Garis border luar.
            new EmptyBorder(20, 20, 20, 20) // Padding di dalam border.
        ));
        setLayout(new GridBagLayout()); // Menggunakan layout manager GridBagLayout.
        GridBagConstraints gbc = new GridBagConstraints(); // Objek untuk mengatur properti penempatan komponen.
        gbc.insets = new Insets(10, 10, 10, 10); // Jarak (padding) antar komponen.
        gbc.fill = GridBagConstraints.HORIZONTAL; // Komponen akan mengisi ruang secara horizontal.
        gbc.anchor = GridBagConstraints.WEST; // Komponen akan rata kiri dalam sel gridnya.

        // --- Bagian Label dan Field untuk ID Tiket ---
        // ID Tiket akan di-generate otomatis dan ditampilkan, tidak bisa diedit pengguna.
        gbc.gridx = 0; gbc.gridy = 0; // Posisi komponen pada grid (kolom 0, baris 0).
        JLabel lblIdTiketMasuk = new JLabel("ID Tiket:"); applyCommonStyling(lblIdTiketMasuk);
        add(lblIdTiketMasuk, gbc); // Menambahkan label ke panel.
        gbc.gridx = 1; gbc.gridwidth = 2; // Field ID Tiket akan menempati 2 kolom.
        tfIdTiketMasuk = new JTextField(20); // Inisialisasi JTextField.
        tfIdTiketMasuk.setEditable(false); // Membuat field tidak bisa diedit.
        applyCommonStyling(tfIdTiketMasuk); // Menerapkan style umum.
        add(tfIdTiketMasuk, gbc); // Menambahkan field ke panel.

        // --- Bagian Label dan Field untuk Plat Nomor ---
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; // Reset gridwidth ke 1 untuk label.
        JLabel lblPlatMasuk = new JLabel("Plat Nomor:"); applyCommonStyling(lblPlatMasuk);
        add(lblPlatMasuk, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; // Field Plat Nomor menempati 2 kolom.
        tfPlatNomorMasuk = new JTextField(20); applyCommonStyling(tfPlatNomorMasuk);
        add(tfPlatNomorMasuk, gbc);

        // --- Bagian Label dan Radio Button untuk Jenis Kendaraan ---
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel lblJenisMasuk = new JLabel("Jenis Kendaraan:"); applyCommonStyling(lblJenisMasuk);
        add(lblJenisMasuk, gbc);

        // Panel internal untuk mengelompokkan radio button jenis kendaraan.
        JPanel panelRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); // Menggunakan FlowLayout rata kiri.
        panelRadio.setBackground(colorPanel); // Sesuaikan warna latar belakang panel radio.
        rbMobilMasuk = new JRadioButton("Mobil"); applyCommonStyling(rbMobilMasuk);
        rbMobilMasuk.setSelected(true); // Opsi "Mobil" dipilih secara default.
        rbMobilBoxMasuk = new JRadioButton("Mobil Box"); applyCommonStyling(rbMobilBoxMasuk);
        rbMotorMasuk = new JRadioButton("Motor"); applyCommonStyling(rbMotorMasuk);
        // ButtonGroup memastikan hanya satu JRadioButton yang bisa dipilih dalam satu waktu.
        bgJenisKendaraanMasuk = new ButtonGroup();
        bgJenisKendaraanMasuk.add(rbMobilMasuk);
        bgJenisKendaraanMasuk.add(rbMobilBoxMasuk);
        bgJenisKendaraanMasuk.add(rbMotorMasuk);
        panelRadio.add(rbMobilMasuk);
        panelRadio.add(rbMobilBoxMasuk);
        panelRadio.add(rbMotorMasuk);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2; // Panel radio menempati 2 kolom.
        add(panelRadio, gbc);

        // --- Bagian Label dan Field untuk Jam Masuk ---
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        JLabel lblJamMasuk = new JLabel("Jam Masuk (HH:mm):"); applyCommonStyling(lblJamMasuk);
        add(lblJamMasuk, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; // Field Jam Masuk menempati 2 kolom.
        tfJamMasuk = new JTextField(20); applyCommonStyling(tfJamMasuk);
        add(tfJamMasuk, gbc);

        // --- Bagian Panel Tombol Aksi (Simpan dan Hapus) ---
        JPanel panelTombolMasuk = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // Tombol ditata di tengah.
        panelTombolMasuk.setBackground(colorPanel);
        simpanButtonMasuk = new JButton("Simpan Tiket Masuk"); applyCommonStyling(simpanButtonMasuk);
        hapusButtonMasuk = new JButton("Hapus Form"); applyCommonStyling(hapusButtonMasuk);
        panelTombolMasuk.add(simpanButtonMasuk);
        panelTombolMasuk.add(hapusButtonMasuk);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3; // Panel tombol menempati 3 kolom.
        gbc.fill = GridBagConstraints.NONE; // Ukuran panel tombol tidak mengisi sel.
        gbc.anchor = GridBagConstraints.CENTER; // Panel tombol diposisikan di tengah sel.
        gbc.insets = new Insets(20, 8, 8, 8); // Padding atas lebih besar untuk jarak.
        add(panelTombolMasuk, gbc);

        // Menambahkan ActionListener ke masing-masing tombol.
        simpanButtonMasuk.addActionListener(_ -> simpanTiketMasuk()); // Lambda expression untuk aksi simpan.
        hapusButtonMasuk.addActionListener(_ -> clearFormMasuk()); // Lambda expression untuk aksi hapus form.

        gbc.gridy = 5; gbc.weighty = 1.0; // Komponen dummy ini akan mengambil sisa ruang vertikal,
                                          // mendorong semua komponen lain ke bagian atas panel.
        add(new JLabel(), gbc); // Menambahkan komponen dummy (kosong).
    }

    /**
     * Menerapkan styling umum ke komponen UI (JTextField, JButton, JLabel, JRadioButton)
     * untuk menjaga konsistensi tampilan.
     * @param component Komponen JComponent yang akan diberi style.
     */
    private void applyCommonStyling(JComponent component) {
        // Logika styling untuk JTextField, dibedakan antara editable dan read-only.
        if (component instanceof JTextField) {
            JTextField textField = (JTextField) component;
            textField.setFont(new Font("Arial", Font.PLAIN, 14));
            if (textField.isEditable()) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(colorInputBorder, 1),
                    new EmptyBorder(5, 5, 5, 5)
                ));
                textField.setBackground(new Color(245, 250, 255));
            } else { // Styling khusus untuk field yang tidak bisa diedit (read-only).
                 textField.setBackground(colorReadOnlyBg);
                 textField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(200, 200, 200), 1),
                    new EmptyBorder(5, 5, 5, 5)
                ));
                textField.setForeground(colorReadOnlyText);
            }
        // Logika styling untuk JButton, termasuk efek hover sederhana (warna berubah saat mouse diatasnya).
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.setBackground(colorButton);
            button.setForeground(colorButtonText);
            button.setFocusPainted(false); // Menghilangkan border fokus default.
            button.setBorder(new EmptyBorder(8, 15, 8, 15)); // Padding di dalam tombol.
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(colorButton.darker()); // Warna lebih gelap saat mouse masuk.
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(colorButton); // Warna kembali normal saat mouse keluar.
                }
            });
        // Logika styling untuk JLabel.
        } else if (component instanceof JLabel) {
            component.setFont(new Font("Arial", Font.PLAIN, 14));
            component.setForeground(colorLabelText);
        // Logika styling untuk JRadioButton.
        } else if (component instanceof JRadioButton) {
            component.setFont(new Font("Arial", Font.PLAIN, 14));
            component.setForeground(colorLabelText);
            component.setBackground(colorPanel); // Menyamakan background radio button dengan panel.
        }
    }

    /**
     * Menghasilkan ID tiket baru secara sekuensial (misal: PK001, PK002, dst.).
     * Menggunakan nilai `nomorTiketCounter` yang sudah diinisialisasi dari database.
     * @return String ID tiket yang baru dengan format "PK" diikuti 3 digit angka.
     */
    private String generateIdTiket() {
        nomorTiketCounter++; // Menaikkan counter untuk ID tiket berikutnya.
        return String.format("PK%03d", nomorTiketCounter); // Memformat nomor menjadi string "PKXXX".
    }

    /**
     * Memproses penyimpanan data tiket parkir masuk ke database.
     * Langkah-langkah: validasi input, generate ID tiket, cek duplikasi plat aktif,
     * simpan ke DB, tampilkan struk, dan panggil listener jika ada.
     */
    private void simpanTiketMasuk() {
        // Mengambil input dari field dan melakukan normalisasi (trim, uppercase).
        String platNomor = tfPlatNomorMasuk.getText().trim().toUpperCase();
        tfPlatNomorMasuk.setText(platNomor); // Mengupdate field dengan nilai yang sudah dinormalisasi.
        String jamMasuk = tfJamMasuk.getText().trim();
        String jenisKendaraan = "";

        // Menentukan jenis kendaraan berdasarkan pilihan radio button.
        if (rbMobilMasuk.isSelected()) jenisKendaraan = "Mobil";
        else if (rbMobilBoxMasuk.isSelected()) jenisKendaraan = "Mobil Box";
        else if (rbMotorMasuk.isSelected()) jenisKendaraan = "Motor";
        else { // Jika tidak ada jenis kendaraan yang dipilih (seharusnya tidak terjadi karena ada default).
            JOptionPane.showMessageDialog(this, "Pilih jenis kendaraan!", "Error Alert!", JOptionPane.ERROR_MESSAGE);
            return; // Menghentikan proses.
        }

        // Validasi input: plat nomor dan jam masuk tidak boleh kosong.
        if (platNomor.isEmpty() || jamMasuk.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Plat nomor dan jam masuk tidak boleh kosong!", "Error Alert!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Validasi format jam masuk harus HH:mm (misal, 09:30).
        if (!jamMasuk.matches("\\d{2}:\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Format jam masuk salah! Gunakan HH:mm.", "Error Alert!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Validasi nilai jam (00-23) dan menit (00-59).
        try {
            String[] parts = jamMasuk.split(":");
            int jam = Integer.parseInt(parts[0]);
            int menit = Integer.parseInt(parts[1]);
            if (jam < 0 || jam > 23 || menit < 0 || menit > 59) {
                 JOptionPane.showMessageDialog(this, "Jam atau menit tidak valid! Gunakan HH:mm (00-23 : 00-59).", "Error Alert!", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) { // Menangani jika format tidak valid.
            JOptionPane.showMessageDialog(this, "Format jam masuk salah! Gunakan HH:mm.", "Error Alert!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Generate ID tiket baru dan menampilkannya di field ID Tiket.
        String idTiket = generateIdTiket();
        tfIdTiketMasuk.setText(idTiket);

        // Blok try-with-resources untuk koneksi database.
        try (Connection conn = dbParkir.getConnection()) {
            // Query untuk memeriksa apakah kendaraan dengan plat nomor tersebut sudah parkir dan belum keluar.
            String checkSql = "SELECT COUNT(*) FROM parkiran WHERE plat_nomor = ? AND jam_keluar IS NULL";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, platNomor);
                ResultSet rs = checkStmt.executeQuery(); // Menggunakan ResultSet secara eksplisit.
                if (rs.next() && rs.getInt(1) > 0) { // Jika ditemukan kendaraan aktif dengan plat nomor sama.
                    JOptionPane.showMessageDialog(this, "Kendaraan dengan plat nomor ini sudah tercatat parkir aktif.", "Error Registrasi", JOptionPane.ERROR_MESSAGE);
                    tfIdTiketMasuk.setText(""); // Kosongkan ID tiket yang terlanjur di-generate.
                    nomorTiketCounter--; // Rollback counter karena ID tidak jadi digunakan.
                    return; // Hentikan proses.
                }
            }

            // Query SQL untuk memasukkan data parkir baru ke tabel 'parkiran'.
            String sql = "INSERT INTO parkiran (id_tiket, plat_nomor, jenis_kendaraan, jam_masuk) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, idTiket);
                stmt.setString(2, platNomor);
                stmt.setString(3, jenisKendaraan);
                stmt.setString(4, jamMasuk);

                int affectedRows = stmt.executeUpdate(); // Menjalankan query INSERT.
                if (affectedRows > 0) { // Jika data berhasil dimasukkan (baris terpengaruh > 0).
                    // --- PEMBUATAN DAN PENAMPILAN STRUK TIKET MASUK ---
                    // Membuat format struk tiket masuk.
                    String strukMasukOutput = String.format(
                        "====== TIKET PARKIR MASUK ======\n" +
                        "ID Tiket       : %s\n" +
                        "Plat Nomor     : %s\n" +
                        "Jenis Kendaraan: %s\n" +
                        "Jam Masuk      : %s\n" +
                        "===============================\n" +
                        "  Harap simpan tiket ini dengan baik.\n" +
                        "      Selamat Parkir di ParkirIn!\n" +
                        "===============================",
                        idTiket, platNomor, jenisKendaraan, jamMasuk
                    );
                    // Menampilkan struk dalam JTextArea di dalam JOptionPane agar format teks terjaga.
                    JTextArea textAreaStruk = new JTextArea(strukMasukOutput);
                    textAreaStruk.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Font monospaced untuk tampilan struk.
                    textAreaStruk.setEditable(false); // Struk tidak bisa diedit.
                    JScrollPane scrollPaneStruk = new JScrollPane(textAreaStruk); // Jika struk panjang, bisa di-scroll.
                    scrollPaneStruk.setPreferredSize(new Dimension(350, 200)); // Ukuran dialog struk.
                    
                    JOptionPane.showMessageDialog(this, scrollPaneStruk, "Tiket Parkir Masuk - ParkirIn", JOptionPane.INFORMATION_MESSAGE);

                    clearFormMasuk(); // Mengosongkan form setelah data berhasil disimpan dan struk ditampilkan.

                    // Panggil listener (jika ada) untuk memberitahu bahwa data telah diperbarui.
                    // Ini berguna untuk me-refresh panel riwayat secara otomatis.
                    if (dataUpdateListener != null) {
                        dataUpdateListener.onDataNeedsRefresh();
                    }
                } else { // Jika INSERT gagal (seharusnya jarang terjadi jika tidak ada error constraint).
                    JOptionPane.showMessageDialog(this, "Gagal menyimpan data parkir masuk.", "Error Registrasi", JOptionPane.ERROR_MESSAGE);
                    tfIdTiketMasuk.setText(""); // Kosongkan ID tiket.
                    nomorTiketCounter--; // Rollback counter karena ID tidak jadi digunakan.
                }
            }
        } catch (SQLException ex) { // Menangani error terkait database.
            JOptionPane.showMessageDialog(this, "Database error (Registrasi): " + ex.getMessage(), "Error Registrasi", JOptionPane.ERROR_MESSAGE);
            tfIdTiketMasuk.setText("");
            nomorTiketCounter--; // Rollback counter.
            ex.printStackTrace(); // Cetak stack trace untuk debugging.
        } catch (Exception ex) { // Menangani error umum lainnya.
            JOptionPane.showMessageDialog(this, "General error (Registrasi): " + ex.getMessage(), "Error Registrasi", JOptionPane.ERROR_MESSAGE);
            tfIdTiketMasuk.setText("");
             nomorTiketCounter--; // Rollback counter.
            ex.printStackTrace();
        }
    }

    /**
     * Mengosongkan semua field input pada form parkir masuk dan memberi fokus
     * kembali ke field plat nomor untuk input data berikutnya.
     */
    private void clearFormMasuk() {
        tfIdTiketMasuk.setText(""); // ID tiket akan di-generate ulang untuk entri berikutnya.
        tfPlatNomorMasuk.setText("");
        tfJamMasuk.setText("");
        rbMobilMasuk.setSelected(true); // Mengembalikan pilihan default ke "Mobil".
        tfPlatNomorMasuk.requestFocus(); // Memberi fokus ke field plat nomor.
    }
} 