package UAS.Tampilan; // Mendefinisikan paket tempat kelas ini berada

import javax.swing.*; // Mengimpor kelas-kelas Swing untuk GUI
import javax.swing.border.EmptyBorder; // Mengimpor kelas EmptyBorder untuk padding
import javax.swing.border.LineBorder; // Mengimpor kelas LineBorder untuk garis tepi
import javax.swing.border.TitledBorder; // Mengimpor kelas TitledBorder untuk judul pada border
import java.awt.*; // Mengimpor kelas-kelas AWT untuk grafis dan UI dasar
import java.sql.Connection; // Mengimpor Connection untuk koneksi SQL
import java.sql.PreparedStatement; // Mengimpor PreparedStatement untuk query SQL yang aman
import java.sql.ResultSet; // Mengimpor ResultSet untuk menampung hasil query SQL
import java.sql.SQLException; // Mengimpor SQLException untuk menangani error SQL
import java.time.LocalTime; // Mengimpor LocalTime untuk mendapatkan waktu saat ini
import java.time.format.DateTimeFormatter; // Mengimpor DateTimeFormatter untuk memformat waktu

// Mengimpor kelas-kelas yang dibutuhkan dari paket lain
import UAS.databaseParkir.dbParkir; // Untuk koneksi ke database
import UAS.macamKendaraan.*; // Untuk kelas-kelas jenis kendaraan (Mobil, Motor, dll.)
// Import DataUpdateListener jika berada di paket yang sama atau paket lain
// import UAS.Tampilan.DataUpdateListener; // Asumsi berada di paket UAS.Tampilan

/**
 * Kelas PanelParkirKeluar adalah JPanel yang menangani semua fungsionalitas
 * terkait proses kendaraan keluar dari area parkir, termasuk pencarian data,
 * perhitungan biaya, pencetakan struk, dan pembaruan data di database.
 */
public class PanelParkirKeluar extends JPanel {

    // === KOMPONEN UI ===
    // Field untuk mencari tiket berdasarkan ID saat kendaraan akan keluar
    private JTextField tfIdTiketCariKeluar;
    // Field-field read-only untuk menampilkan detail parkir setelah data ditemukan
    private JTextField tfPlatNomorKeluar, tfIdTiketKeluar, tfJenisKendaraanKeluar, tfJamMasukKeluar,
                       tfJamKeluarKeluar, tfLamaParkirKeluar, tfTotalPembayaranKeluar;
    // Tombol-tombol aksi
    private JButton cariButtonKeluar, prosesCetakButtonKeluar, hapusButtonKeluar;

    // === VARIABEL PENYIMPAN DATA DARI DATABASE ===
    // Digunakan untuk menyimpan data sementara dari database untuk proses keluar
    private String dbIdTiketKeluar;
    private String dbJenisKendaraanKeluar;
    private String dbJamMasukKeluar_val;
    private String dbPlatNomorKeluar_val;

    // Variabel untuk listener pembaruan data (untuk memberitahu panel riwayat)
    private DataUpdateListener dataUpdateListener;

    // === DEFINISI WARNA UMUM UNTUK UI ===
    private Color colorPanel = new Color(255, 255, 255);
    private Color colorHeaderBorder = new Color(70, 130, 180);
    private Color colorButton = new Color(70, 130, 180);
    private Color colorButtonText = Color.WHITE;
    private Color colorInputBorder = new Color(173, 216, 230);
    private Color colorLabelText = new Color(50, 50, 50);
    private Color colorReadOnlyBg = new Color(235,235,235);
    private Color colorReadOnlyText = new Color(80,80,80);

    /**
     * Konstruktor PanelParkirKeluar.
     * Memanggil initComponents untuk membangun antarmuka pengguna.
     */
    public PanelParkirKeluar() {
        initComponents();
    }

    /**
     * Metode untuk mengatur listener pembaruan data.
     * Dipanggil dari luar (misalnya oleh MainApplication) untuk mendaftarkan listener.
     * @param listener Objek DataUpdateListener yang akan diberitahu saat ada perubahan data.
     */
    public void setDataUpdateListener(DataUpdateListener listener) {
        this.dataUpdateListener = listener;
    }

    /**
     * Menginisialisasi dan menata semua komponen antarmuka pengguna (UI) pada panel ini.
     */
    private void initComponents() {
        setBackground(colorPanel); // Mengatur warna latar belakang panel utama
        // Mengatur border panel utama dengan garis dan padding
        setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(colorHeaderBorder, 2, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        setLayout(new GridBagLayout()); // Menggunakan GridBagLayout untuk penataan fleksibel
        GridBagConstraints gbc = new GridBagConstraints(); // Objek untuk mengatur penempatan komponen
        gbc.insets = new Insets(8, 8, 8, 8); // Jarak antar komponen

        // --- Bagian Input ID Tiket untuk Pencarian ---
        gbc.gridx = 0; gbc.gridy = 0; // Posisi awal
        gbc.weightx = 0; // Tidak dapat merenggang secara horizontal secara default
        gbc.fill = GridBagConstraints.NONE; // Ukuran komponen tidak mengisi sel
        gbc.anchor = GridBagConstraints.EAST; // Rata kanan dalam sel
        JLabel lblIdTiketCari = new JLabel("ID Tiket Kendaraan:"); applyCommonStyling(lblIdTiketCari);
        add(lblIdTiketCari, gbc);

        tfIdTiketCariKeluar = new JTextField(15); applyCommonStyling(tfIdTiketCariKeluar);
        gbc.gridx = 1; // Kolom berikutnya
        gbc.weightx = 1.0; // Field dapat merenggang mengisi sisa ruang horizontal
        gbc.fill = GridBagConstraints.HORIZONTAL; // Mengisi sel secara horizontal
        gbc.anchor = GridBagConstraints.WEST; // Rata kiri
        gbc.ipady = 2; // Padding internal vertikal untuk field
        add(tfIdTiketCariKeluar, gbc);

        cariButtonKeluar = new JButton("Cari"); applyCommonStyling(cariButtonKeluar);
        gbc.gridx = 2; // Kolom berikutnya
        gbc.weightx = 0; // Tombol tidak merenggang
        gbc.fill = GridBagConstraints.NONE; // Ukuran tombol tetap
        gbc.ipady = 0; // Reset padding internal vertikal
        add(cariButtonKeluar, gbc);

        // Mengembalikan pengaturan fill dan anchor untuk komponen selanjutnya
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0;


        // --- Bagian Panel Detail Parkir ---
        // Panel ini akan menampilkan detail kendaraan yang ditemukan.
        JPanel detailPanel = new JPanel(new GridBagLayout());
        detailPanel.setBackground(colorPanel);
        // Membuat TitledBorder untuk memberi judul pada sekelompok field detail
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(colorHeaderBorder, 1), // Garis border
            "Detail Parkir", // Judul border
            TitledBorder.DEFAULT_JUSTIFICATION, // Penjajaran judul default
            TitledBorder.DEFAULT_POSITION, // Posisi judul default
            new Font("Arial", Font.BOLD, 14), // Font judul
            colorHeaderBorder // Warna teks judul
        );
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
            titledBorder,
            new EmptyBorder(10,10,10,10) // Padding di dalam TitledBorder
        ));

        GridBagConstraints gbcDetail = new GridBagConstraints(); // Constraints untuk komponen di dalam detailPanel
        gbcDetail.insets = new Insets(5,5,5,5);
        gbcDetail.fill = GridBagConstraints.HORIZONTAL;
        gbcDetail.anchor = GridBagConstraints.WEST;

        // Menambahkan baris-baris detail (Label dan TextField read-only)
        // Metode setupDetailRow digunakan untuk kemudahan
        setupDetailRow(detailPanel, gbcDetail, 0, "ID Tiket:", tfIdTiketKeluar = createReadOnlyTextField());
        setupDetailRow(detailPanel, gbcDetail, 1, "Plat Nomor:", tfPlatNomorKeluar = createReadOnlyTextField());
        setupDetailRow(detailPanel, gbcDetail, 2, "Jenis Kendaraan:", tfJenisKendaraanKeluar = createReadOnlyTextField());
        setupDetailRow(detailPanel, gbcDetail, 3, "Jam Masuk:", tfJamMasukKeluar = createReadOnlyTextField());
        setupDetailRow(detailPanel, gbcDetail, 4, "Jam Keluar:", tfJamKeluarKeluar = createReadOnlyTextField());
        setupDetailRow(detailPanel, gbcDetail, 5, "Lama Parkir:", tfLamaParkirKeluar = createReadOnlyTextField());
        setupDetailRow(detailPanel, gbcDetail, 6, "Total Pembayaran:", tfTotalPembayaranKeluar = createReadOnlyTextField());

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3; // Panel detail memanjang selebar 3 kolom
        gbc.insets = new Insets(15, 8, 15, 8); // Padding untuk panel detail
        add(detailPanel, gbc);

        // --- Bagian Panel Tombol Aksi Keluar ---
        JPanel panelTombolKeluar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // Tombol di tengah
        panelTombolKeluar.setBackground(colorPanel);
        prosesCetakButtonKeluar = new JButton("Proses & Cetak Struk"); applyCommonStyling(prosesCetakButtonKeluar);
        hapusButtonKeluar = new JButton("Hapus Form"); applyCommonStyling(hapusButtonKeluar);
        panelTombolKeluar.add(prosesCetakButtonKeluar);
        panelTombolKeluar.add(hapusButtonKeluar);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3; // Panel tombol juga memanjang 3 kolom
        gbc.fill = GridBagConstraints.NONE; // Ukuran panel tombol tidak merenggang
        gbc.anchor = GridBagConstraints.CENTER; // Panel tombol di tengah
        gbc.insets = new Insets(10, 8, 8, 8);
        add(panelTombolKeluar, gbc);

        // Menambahkan ActionListener ke tombol-tombol
        cariButtonKeluar.addActionListener(_ -> cariKendaraanKeluar());
        prosesCetakButtonKeluar.addActionListener(_ -> prosesCetakKeluar());
        hapusButtonKeluar.addActionListener(_ -> clearFormKeluar());

        gbc.gridy = 3; gbc.weighty = 1.0; // Memberi sisa ruang vertikal ke komponen dummy di bawah
        add(new JLabel(), gbc); // Komponen dummy untuk mendorong konten ke atas
    }

    /**
     * Menerapkan styling umum ke komponen UI (JTextField, JButton, JLabel).
     * @param component Komponen yang akan diberi style.
     */
    private void applyCommonStyling(JComponent component) {
        // Logika styling untuk JTextField (editable dan read-only)
        if (component instanceof JTextField) {
            JTextField textField = (JTextField) component;
            textField.setFont(new Font("Arial", Font.PLAIN, 14));
            if (textField.isEditable()) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(colorInputBorder, 1),
                    new EmptyBorder(5, 5, 5, 5)
                ));
                textField.setBackground(new Color(245, 250, 255));
            } else { // Styling untuk field read-only
                 textField.setBackground(colorReadOnlyBg);
                 textField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(200, 200, 200), 1),
                    new EmptyBorder(5, 5, 5, 5)
                ));
                textField.setForeground(colorReadOnlyText);
            }
        // Logika styling untuk JButton (termasuk efek hover)
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.setBackground(colorButton);
            button.setForeground(colorButtonText);
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(8, 15, 8, 15));
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(colorButton.darker());
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(colorButton);
                }
            });
        // Logika styling untuk JLabel
        } else if (component instanceof JLabel) {
            component.setFont(new Font("Arial", Font.PLAIN, 14));
            component.setForeground(colorLabelText);
        }
    }

    /**
     * Membuat JTextField yang tidak dapat diedit (read-only) dengan styling umum.
     * @return JTextField yang sudah di-style dan read-only.
     */
    private JTextField createReadOnlyTextField() {
        JTextField textField = new JTextField(20);
        textField.setEditable(false); // Kunci: membuat field tidak bisa diedit
        applyCommonStyling(textField); // Menerapkan style umum
        return textField;
    }

    /**
     * Metode helper untuk menata sepasang JLabel dan JTextField dalam satu baris
     * di dalam panel detail.
     * @param parent Panel tempat komponen akan ditambahkan.
     * @param gbc GridBagConstraints yang digunakan.
     * @param yPos Posisi baris (gridy).
     * @param labelText Teks untuk JLabel.
     * @param textField JTextField yang akan ditambahkan.
     */
    private void setupDetailRow(JPanel parent, GridBagConstraints gbc, int yPos, String labelText, JTextField textField) {
        gbc.gridx = 0; gbc.gridy = yPos; gbc.weightx = 0.3; // Label mengambil 30% lebar
        JLabel label = new JLabel(labelText); applyCommonStyling(label);
        parent.add(label, gbc);
        gbc.gridx = 1; gbc.gridy = yPos; gbc.weightx = 0.7; // Field mengambil 70% lebar
        parent.add(textField, gbc);
    }

    /**
     * Mengosongkan field-field detail parkir dan variabel data terkait.
     */
    private void clearFormKeluarFieldsOnly() {
        tfIdTiketKeluar.setText("");
        tfPlatNomorKeluar.setText("");
        tfJenisKendaraanKeluar.setText("");
        tfJamMasukKeluar.setText("");
        tfJamKeluarKeluar.setText("");
        tfLamaParkirKeluar.setText("");
        tfTotalPembayaranKeluar.setText("");
        // Mereset variabel internal yang menyimpan data dari DB
        dbIdTiketKeluar = null;
        dbJenisKendaraanKeluar = null;
        dbJamMasukKeluar_val = null;
        dbPlatNomorKeluar_val = null;
    }

    /**
     * Mengosongkan seluruh form parkir keluar, termasuk field pencarian ID tiket.
     * Memberi fokus kembali ke field pencarian.
     */
    private void clearFormKeluar() {
        tfIdTiketCariKeluar.setText(""); // Mengosongkan field pencarian
        clearFormKeluarFieldsOnly(); // Mengosongkan field detail
        tfIdTiketCariKeluar.requestFocus(); // Memberi fokus ke field pencarian
    }

    /**
     * Mencari data kendaraan yang masih aktif parkir di database berdasarkan ID Tiket.
     * Jika ditemukan, data akan ditampilkan di field detail dan biaya awal dihitung.
     */
    private void cariKendaraanKeluar() {
        String idTiketInput = tfIdTiketCariKeluar.getText().trim().toUpperCase(); // Ambil ID tiket, ubah ke uppercase
        tfIdTiketCariKeluar.setText(idTiketInput); // Set kembali ke field (jika ada perubahan case)

        if (idTiketInput.isEmpty()) { // Validasi input tidak kosong
            JOptionPane.showMessageDialog(this, "Masukkan ID Tiket untuk pencarian.", "Info Proses Keluar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        clearFormKeluarFieldsOnly(); // Kosongkan field detail sebelum pencarian baru

        // Blok try-with-resources untuk koneksi database
        try (Connection conn = dbParkir.getConnection()) {
            // Query untuk mencari kendaraan berdasarkan ID Tiket yang jam keluarnya masih NULL (aktif)
            String sql = "SELECT id_tiket, plat_nomor, jenis_kendaraan, jam_masuk FROM parkiran WHERE id_tiket = ? AND jam_keluar IS NULL";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, idTiketInput); // Set parameter ID Tiket
                ResultSet rs = stmt.executeQuery(); // Eksekusi query

                if (rs.next()) { // Jika data ditemukan
                    // Ambil data dari ResultSet dan simpan ke variabel instance
                    dbIdTiketKeluar = rs.getString("id_tiket");
                    dbPlatNomorKeluar_val = rs.getString("plat_nomor");
                    dbJenisKendaraanKeluar = rs.getString("jenis_kendaraan");
                    dbJamMasukKeluar_val = rs.getString("jam_masuk");

                    // Tampilkan data ke field-field di UI
                    tfIdTiketKeluar.setText(dbIdTiketKeluar);
                    tfPlatNomorKeluar.setText(dbPlatNomorKeluar_val);
                    tfJenisKendaraanKeluar.setText(dbJenisKendaraanKeluar);
                    tfJamMasukKeluar.setText(dbJamMasukKeluar_val);

                    // Atur jam keluar saat ini (sebagai estimasi awal)
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    LocalTime now = LocalTime.now();
                    tfJamKeluarKeluar.setText(now.format(timeFormatter));

                    // Hitung dan tampilkan detail lama parkir dan biaya berdasarkan data yang ada
                    calculateAndDisplayParkingDetailsKeluar(dbJamMasukKeluar_val, now.format(timeFormatter), dbJenisKendaraanKeluar);
                } else { // Jika data tidak ditemukan atau kendaraan sudah keluar
                    JOptionPane.showMessageDialog(this, "ID Tiket tidak ditemukan atau kendaraan sudah keluar.", "Info Proses Keluar", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException ex) { // Tangani error SQL
            JOptionPane.showMessageDialog(this, "Database error saat mencari (Proses Keluar): " + ex.getMessage(), "Error Proses Keluar", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (Exception ex) { // Tangani error umum lainnya
            JOptionPane.showMessageDialog(this, "Error saat mencari (Proses Keluar): " + ex.getMessage(), "Error Proses Keluar", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Menghitung dan menampilkan lama parkir serta total biaya parkir berdasarkan
     * jam masuk, jam keluar, dan jenis kendaraan.
     */
    private void calculateAndDisplayParkingDetailsKeluar(String jamMasukStr, String jamKeluarStr, String jenisKendaraanStr) {
        try {
            // Parsing string waktu ke komponen jam dan menit
            String[] masukParts = jamMasukStr.split(":");
            String[] keluarParts = jamKeluarStr.split(":");

            int jamMasukVal = Integer.parseInt(masukParts[0]);
            int menitMasukVal = Integer.parseInt(masukParts[1]);
            int jamKeluarVal = Integer.parseInt(keluarParts[0]);
            int menitKeluarVal = Integer.parseInt(keluarParts[1]);

            // Konversi waktu ke total menit dari awal hari
            int totalMasukMenit = jamMasukVal * 60 + menitMasukVal;
            int totalKeluarMenit = jamKeluarVal * 60 + menitKeluarVal;

            int lamaMenit;
            if (totalKeluarMenit < totalMasukMenit) { // Jika jam keluar lebih kecil (kendaraan parkir melewati tengah malam)
                lamaMenit = (24 * 60 - totalMasukMenit) + totalKeluarMenit; // Hitung durasi melewati hari
            } else { // Jika parkir di hari yang sama
                lamaMenit = totalKeluarMenit - totalMasukMenit;
            }
            tfLamaParkirKeluar.setText(lamaMenit + " menit"); // Tampilkan lama parkir

            // Tentukan objek Kendaraan berdasarkan jenisnya untuk perhitungan biaya
            Kendaraan kendaraan;
            if ("Mobil".equalsIgnoreCase(jenisKendaraanStr)) kendaraan = new Mobil();
            else if ("Mobil Box".equalsIgnoreCase(jenisKendaraanStr)) kendaraan = new mobilBox();
            else if ("Motor".equalsIgnoreCase(jenisKendaraanStr)) kendaraan = new Motor();
            else { // Jika jenis kendaraan tidak dikenal
                JOptionPane.showMessageDialog(this, "Jenis kendaraan tidak dikenal: " + jenisKendaraanStr, "Error Kalkulasi", JOptionPane.ERROR_MESSAGE);
                tfLamaParkirKeluar.setText("Error");
                tfTotalPembayaranKeluar.setText("Error");
                return;
            }
            // Hitung biaya parkir menggunakan metode dari objek Kendaraan
            int totalBayar = kendaraan.hitungBiayaParkir(lamaMenit);
            tfTotalPembayaranKeluar.setText("Rp " + totalBayar); // Tampilkan total biaya

        } catch (NumberFormatException ex) { // Tangani error jika format waktu salah
            JOptionPane.showMessageDialog(this, "Format waktu salah. Tidak dapat menghitung.", "Error Parsing Waktu", JOptionPane.ERROR_MESSAGE);
            tfLamaParkirKeluar.setText("Error");
            tfTotalPembayaranKeluar.setText("Error");
        } catch (Exception ex) { // Tangani error kalkulasi umum
            JOptionPane.showMessageDialog(this, "Error kalkulasi (Proses Keluar): " + ex.getMessage(), "Error Kalkulasi", JOptionPane.ERROR_MESSAGE);
            tfLamaParkirKeluar.setText("Error");
            tfTotalPembayaranKeluar.setText("Error");
            ex.printStackTrace();
        }
    }

    /**
     * Memproses kendaraan keluar: menghitung biaya final, mencetak struk (dalam bentuk dialog),
     * dan mengupdate data di database.
     */
    private void prosesCetakKeluar() {
        // Validasi: pastikan data kendaraan sudah dicari dan ditemukan
        if (dbIdTiketKeluar == null || dbPlatNomorKeluar_val == null || dbJamMasukKeluar_val == null || dbJenisKendaraanKeluar == null) {
            JOptionPane.showMessageDialog(this, "Cari kendaraan berdasarkan ID Tiket terlebih dahulu atau data tidak lengkap.", "Error Proses Keluar", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Menggunakan waktu saat ini sebagai jam keluar final
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime finalJamKeluarTime = LocalTime.now();
        String finalJamKeluarStr = finalJamKeluarTime.format(timeFormatter);

        int finalLamaMenit;
        int finalTotalBayar;

        // Blok try-catch untuk kalkulasi final (serupa dengan calculateAndDisplayParkingDetailsKeluar)
        try {
            // Parsing waktu masuk dari data yang tersimpan (dbJamMasukKeluar_val)
            String[] masukParts = dbJamMasukKeluar_val.split(":");
            // Parsing waktu keluar dari waktu saat ini (finalJamKeluarStr)
            String[] keluarParts = finalJamKeluarStr.split(":");
            int jamMasukVal = Integer.parseInt(masukParts[0]);
            int menitMasukVal = Integer.parseInt(masukParts[1]);
            int jamKeluarVal = Integer.parseInt(keluarParts[0]);
            int menitKeluarVal = Integer.parseInt(keluarParts[1]);
            int totalMasukMenit = jamMasukVal * 60 + menitMasukVal;
            int totalKeluarMenit = jamKeluarVal * 60 + menitKeluarVal;

            // Perhitungan lama parkir final
            if (totalKeluarMenit < totalMasukMenit) finalLamaMenit = (24 * 60 - totalMasukMenit) + totalKeluarMenit;
            else finalLamaMenit = totalKeluarMenit - totalMasukMenit;

            // Perhitungan biaya parkir final berdasarkan jenis kendaraan yang tersimpan
            Kendaraan kendaraan;
            if ("Mobil".equalsIgnoreCase(dbJenisKendaraanKeluar)) kendaraan = new Mobil();
            else if ("Mobil Box".equalsIgnoreCase(dbJenisKendaraanKeluar)) kendaraan = new mobilBox();
            else if ("Motor".equalsIgnoreCase(dbJenisKendaraanKeluar)) kendaraan = new Motor();
            else {
                JOptionPane.showMessageDialog(this, "Jenis kendaraan tidak valid saat finalisasi.", "Error Proses Keluar", JOptionPane.ERROR_MESSAGE);
                return;
            }
            finalTotalBayar = kendaraan.hitungBiayaParkir(finalLamaMenit);

            // Update field UI dengan nilai final
            tfJamKeluarKeluar.setText(finalJamKeluarStr);
            tfLamaParkirKeluar.setText(finalLamaMenit + " menit");
            tfTotalPembayaranKeluar.setText("Rp " + finalTotalBayar);

        } catch (Exception ex) { // Tangani error saat kalkulasi final
            JOptionPane.showMessageDialog(this, "Error final kalkulasi (Proses Keluar): " + ex.getMessage(), "Error Proses Keluar", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return;
        }

        // --- Pembuatan Struk Parkir (dalam bentuk teks) ---
        String strukOutput = String.format(
            "====== STRUK PARKIR ParkirIn ======\n" +
            "ID Tiket: %s\n" +
            "Plat Nomor: %s\n" +
            "Jenis Kendaraan: %s\n" +
            "Jam Masuk: %s\n" +
            "Jam Keluar: %s\n" +
            "Lama Parkir: %d menit\n" +
            "Total Pembayaran: Rp %d\n" +
            "=================================\n" +
            "Terima kasih telah parkir di ParkirIn!",
            dbIdTiketKeluar,
            dbPlatNomorKeluar_val,
            dbJenisKendaraanKeluar, dbJamMasukKeluar_val,
            finalJamKeluarStr, finalLamaMenit, finalTotalBayar
        );
        // Menampilkan struk dalam JTextArea di dalam JOptionPane
        JTextArea textArea = new JTextArea(strukOutput);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Font monospaced agar rapi
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(380, 220)); // Ukuran dialog struk
        JOptionPane.showMessageDialog(this, scrollPane, "Struk Parkir - ParkirIn", JOptionPane.INFORMATION_MESSAGE);

        // --- Update Data ke Database ---
        // Blok try-with-resources untuk koneksi database
        try (Connection conn = dbParkir.getConnection()) {
            // Query SQL untuk mengupdate jam_keluar, lama_parkir, dan total_pembayaran
            // berdasarkan ID Tiket dan memastikan jam_keluar sebelumnya masih NULL
            String sql = "UPDATE parkiran SET jam_keluar = ?, lama_parkir = ?, total_pembayaran = ? WHERE id_tiket = ? AND jam_keluar IS NULL";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, finalJamKeluarStr);
                stmt.setString(2, finalLamaMenit + " menit");
                stmt.setString(3, "Rp " + finalTotalBayar);
                stmt.setString(4, dbIdTiketKeluar);

                int affectedRows = stmt.executeUpdate(); // Eksekusi update
                if (affectedRows > 0) { // Jika update berhasil
                    JOptionPane.showMessageDialog(this, "Data parkir keluar berhasil disimpan.", "Sukses Proses Keluar", JOptionPane.INFORMATION_MESSAGE);
                    
                    // PERUBAHAN: Panggil listener setelah data berhasil diupdate di database
                    if (dataUpdateListener != null) {
                        dataUpdateListener.onDataNeedsRefresh(); // Memberi tahu bahwa data riwayat perlu di-refresh
                    }
                    clearFormKeluar(); // Kosongkan form
                } else { // Jika update gagal (misalnya, ID tiket tidak ditemukan atau sudah diproses)
                    JOptionPane.showMessageDialog(this, "Gagal update data di database. ID Tiket mungkin sudah diproses atau tidak ditemukan.", "Error Proses Keluar", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) { // Tangani error SQL saat menyimpan
            JOptionPane.showMessageDialog(this, "Database error saat menyimpan (Proses Keluar): " + ex.getMessage(), "Error Proses Keluar", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (Exception ex) { // Tangani error umum lainnya saat menyimpan
            JOptionPane.showMessageDialog(this, "General error saat menyimpan (Proses Keluar): " + ex.getMessage(), "Error Proses Keluar", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}