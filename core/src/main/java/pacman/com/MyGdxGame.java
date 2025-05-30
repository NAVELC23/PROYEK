package pacman.com;

// Dalam kelas MyGdxGame.java
import com.badlogic.gdx.Game; // Jika Anda menggunakan Game, bukan ApplicationAdapter
import com.badlogic.gdx.Gdx;
// ...

public class MyGdxGame extends Game { // Atau ApplicationAdapter

    @Override
    public void create () {
        // setScreen(new MainMenuScreen(this)); // Jika menggunakan kelas Game
        // Jika menggunakan ApplicationAdapter, Anda bisa inisialisasi MainMenuScreen di sini
        // dan memanggil render() dari MainMenuScreen di render() utama
        setScreen(new MainMenuScreen(this)); // Mengatur layar awal ke MainMenuScreen
    }

    // Jika Anda menggunakan ApplicationAdapter, Anda perlu memanggil render() dari MainMenuScreen
    // di dalam render() kelas MyGdxGame
    // @Override
    // public void render() {
    //     super.render(); // Penting jika Anda menggunakan Game dan memanggil setScreen()
    // }
}
