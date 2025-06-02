package UAS.Tampilan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import UAS.databaseParkir.dbParkir;

/**
 * Kelas PanelRiwayatParkir adalah JPanel yang bertanggung jawab untuk menampilkan
 * riwayat data parkir kendaraan dalam bentuk tabel.
 * Panel ini juga memiliki kemampuan untuk memuat ulang (refresh) datanya dan menghapus data.
 */
public class PanelRiwayatParkir extends JPanel {

    // === VARIABEL INSTANCE UNTUK KOMPONEN UI ===
    private JTable tabelRiwayat;
    private DefaultTableModel modelTabelRiwayat;
    private JButton btnHapus; // Tombol untuk menghapus data yang dipilih

    // === DEFINISI WARNA UMUM UNTUK UI ===
    private Color colorPanel = new Color(255, 255, 255);
    private Color colorHeaderBorder = new Color(70, 130, 180);

    /**
     * Konstruktor PanelRiwayatParkir.
     * Memanggil initComponents untuk membangun antarmuka pengguna dan
     * refreshData untuk langsung memuat data saat panel dibuat.
     */
    public PanelRiwayatParkir() {
        initComponents();
        refreshData();
    }

    /**
     * Menginisialisasi dan menata semua komponen antarmuka pengguna (UI) pada panel ini,
     * termasuk JTable untuk menampilkan riwayat dan tombol hapus.
     */
    private void initComponents() {
        setBackground(colorPanel);
        setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(colorHeaderBorder, 2, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        setLayout(new BorderLayout());

        // Mendefinisikan nama-nama kolom untuk tabel riwayat
        String[] columnNames = {
            "ID DB", "ID Tiket", "Plat Nomor", "Jenis Kendaraan", 
            "Jam Masuk", "Jam Keluar", "Lama Parkir", "Total Bayar"
        };

        // Menginisialisasi DefaultTableModel
        modelTabelRiwayat = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Menginisialisasi JTable
        tabelRiwayat = new JTable(modelTabelRiwayat);
        tabelRiwayat.setFillsViewportHeight(true);
        tabelRiwayat.setFont(new Font("Arial", Font.PLAIN, 12));
        tabelRiwayat.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tabelRiwayat.setRowHeight(25);
        tabelRiwayat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Pengaturan lebar kolom
        tabelRiwayat.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabelRiwayat.getColumnModel().getColumn(1).setPreferredWidth(80);
        tabelRiwayat.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabelRiwayat.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabelRiwayat.getColumnModel().getColumn(4).setPreferredWidth(80);
        tabelRiwayat.getColumnModel().getColumn(5).setPreferredWidth(80);
        tabelRiwayat.getColumnModel().getColumn(6).setPreferredWidth(100);
        tabelRiwayat.getColumnModel().getColumn(7).setPreferredWidth(100);

        // Membuat JScrollPane untuk tabel
        JScrollPane scrollPane = new JScrollPane(tabelRiwayat);

        // Membuat panel untuk tombol hapus
        JPanel panelTombol = new JPanel();
        panelTombol.setBackground(colorPanel);
        panelTombol.setLayout(new FlowLayout(FlowLayout.RIGHT)); // Tombol diletakkan di kanan
        btnHapus = new JButton("Hapus");
        btnHapus.setFont(new Font("Arial", Font.BOLD, 12));
        btnHapus.setBackground(new Color(220, 53, 69)); // Warna merah untuk tombol hapus
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setFocusPainted(false);
        btnHapus.setPreferredSize(new Dimension(100, 30));
        panelTombol.add(btnHapus);

        // Menambahkan ActionListener untuk tombol hapus
        btnHapus.addActionListener(_ -> hapusData());

        // Menambahkan komponen ke panel utama
        add(scrollPane, BorderLayout.CENTER);
        add(panelTombol, BorderLayout.SOUTH); // Tombol diletakkan di bagian bawah
    }

    /**
     * Metode untuk menghapus data yang dipilih dari tabel dan database.
     * Data dihapus berdasarkan ID yang ada di kolom pertama (ID DB).
     */
    private void hapusData() {
        // Mendapatkan indeks baris yang dipilih
        int selectedRow = tabelRiwayat.getSelectedRow();
        
        // Memeriksa apakah ada baris yang dipilih
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Pilih baris data yang ingin dihapus!", 
                "Peringatan", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Mendapatkan ID dari baris yang dipilih (kolom pertama)
        int id = (int) modelTabelRiwayat.getValueAt(selectedRow, 0);

        // Konfirmasi penghapusan
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin menghapus data dengan ID " + id + "?", 
            "Konfirmasi Hapus", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return; // Batalkan jika pengguna memilih "No"
        }

        // Menghapus data dari database
        try (Connection conn = dbParkir.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM parkiran WHERE id = ?")) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Data berhasil dihapus!", 
                    "Sukses", 
                    JOptionPane.INFORMATION_MESSAGE);
                refreshData(); // Menyegarkan tabel setelah penghapusan
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Gagal menghapus data!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Gagal menghapus data: " + e.getMessage(), 
                "Error Database", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Metode publik untuk memuat ulang (refresh) data dari database ke dalam tabel riwayat.
     */
    public void refreshData() {
        if (modelTabelRiwayat == null) {
            System.err.println("modelTabelRiwayat belum diinisialisasi. Tidak bisa refresh data.");
            return;
        }

        modelTabelRiwayat.setRowCount(0);

        try (Connection conn = dbParkir.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, Id_tiket, plat_nomor, jenis_kendaraan, jam_masuk, jam_keluar, lama_parkir, total_pembayaran FROM parkiran ORDER BY id DESC")) {

            while (rs.next()) {
                modelTabelRiwayat.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("Id_tiket") != null ? rs.getString("Id_tiket") : "-",
                    rs.getString("plat_nomor"),
                    rs.getString("jenis_kendaraan"),
                    rs.getString("jam_masuk"),
                    rs.getString("jam_keluar") != null ? rs.getString("jam_keluar") : "-",
                    rs.getString("lama_parkir") != null ? rs.getString("lama_parkir") : "-",
                    rs.getString("total_pembayaran") != null ? rs.getString("total_pembayaran") : "-"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data riwayat parkir: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }
}