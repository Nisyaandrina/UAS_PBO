package UAS.macamKendaraan; // Mendefinisikan paket tempat kelas ini berada

/**
 * Kelas Kendaraan adalah kelas dasar (superclass) yang merepresentasikan
 * konsep umum kendaraan dalam sistem parkir. Kelas ini memiliki atribut
 * dan metode dasar yang mungkin berlaku untuk semua jenis kendaraan.
 */
public class Kendaraan {
    // Variabel instance yang menyimpan tarif parkir per jam untuk jenis kendaraan ini.
    // Modifier 'protected' berarti variabel ini bisa diakses oleh kelas ini sendiri,
    // kelas dalam paket yang sama, dan kelas turunannya (subclass).
    protected int hargaPerJam;

    /**
     * Menghitung total biaya parkir berdasarkan durasi parkir dalam menit.
     *
     * Aturan perhitungan:
     * - Jika parkir kurang dari 10 menit, biaya parkir adalah 0 (gratis).
     * - Durasi parkir dalam menit akan dibulatkan ke atas menjadi jam.
     * Contoh: 65 menit dihitung sebagai 2 jam.
     * - Durasi parkir maksimal yang dikenakan biaya adalah 5 jam.
     * Jika parkir lebih dari 5 jam, tetap dihitung sebagai 5 jam.
     */
    public int hitungBiayaParkir(int menitParkir) {
        // Jika durasi parkir kurang dari 10 menit, tidak dikenakan biaya (gratis).
        if (menitParkir < 10) {
            return 0; // Biaya parkir adalah 0
        }

        // Menghitung durasi parkir dalam jam, dengan pembulatan ke atas.
        // menitParkir dibagi dengan 60.0 (double) agar hasilnya desimal,
        // kemudian Math.ceil() digunakan untuk membulatkan ke atas ke bilangan bulat terdekat.
        // Hasilnya di-cast menjadi integer.
        // Contoh: 60 menit / 60.0 = 1.0 -> Math.ceil(1.0) = 1 jam
        //         61 menit / 60.0 = 1.016... -> Math.ceil(1.016...) = 2 jam
        int jamParkir = (int) Math.ceil(menitParkir / 60.0);

        // Menerapkan batas maksimal durasi parkir yang dikenakan biaya, yaitu 5 jam.
        // Jika hasil perhitungan jamParkir lebih dari 5 jam, maka akan dianggap 5 jam.
        if (jamParkir > 5) {
            jamParkir = 5; // Batasi maksimal 5 jam
        }

        // Menghitung total biaya parkir dengan mengalikan durasi parkir (dalam jam)
        // dengan tarif per jam (hargaPerJam) yang spesifik untuk jenis kendaraan ini.
        return jamParkir * hargaPerJam;
    }
}