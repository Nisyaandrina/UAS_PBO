package UAS.Tampilan; 
/**
 * Interface DataUpdateListener mendefinisikan sebuah kontrak (perjanjian)
 * untuk kelas-kelas yang ingin "mendengarkan" atau merespons adanya kebutuhan
 * untuk memperbarui (refresh) data.
 *
 * Kelas lain (misalnya, panel yang menampilkan data seperti PanelRiwayatParkir)
 * dapat mengimplementasikan interface ini. Kemudian, kelas yang melakukan perubahan data
 * (misalnya, PanelParkirMasuk atau PanelParkirKeluar) dapat memanggil metode
 * onDataNeedsRefresh() pada objek listener untuk memberi tahu bahwa data telah berubah
 * dan perlu dimuat ulang.
 */
public interface DataUpdateListener {

    /**
     * Metode ini akan dipanggil ketika terjadi suatu aksi yang menyebabkan
     * data (misalnya di database) berubah, sehingga komponen lain yang
     * menampilkan data tersebut perlu memuat ulang (refresh) tampilannya.
     */
    void onDataNeedsRefresh();
}