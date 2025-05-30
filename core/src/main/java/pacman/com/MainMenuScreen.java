package pacman.com;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuScreen implements Screen {

    private MyGdxGame game; // Referensi ke kelas game utama Anda
    private SpriteBatch batch;
    private Texture mainMenuTexture;
    private Stage stage;

    public MainMenuScreen(MyGdxGame game) {
        this.game = game;
        batch = new SpriteBatch();
        stage = new Stage(new ScreenViewport()); // Gunakan ScreenViewport untuk scaling otomatis
    }

    @Override
    public void show() {
        // Muat tekstur gambar menu utama
        mainMenuTexture = new Texture(Gdx.files.internal("MainScreenMenu.png"));

        // Buat objek Image dari tekstur
        Image menuImage = new Image(mainMenuTexture);

        // Atur posisi dan ukuran gambar (opsional, sesuaikan dengan kebutuhan Anda)
        // Contoh: Pusatkan gambar
        menuImage.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // Mengisi seluruh layar
        // Jika ingin mempertahankan aspek rasio dan menyesuaikan lebar/tinggi:
        // float aspectRatio = (float) mainMenuTexture.getWidth() / mainMenuTexture.getHeight();
        // float targetWidth = Gdx.graphics.getWidth();
        // float targetHeight = targetWidth / aspectRatio;
        // if (targetHeight > Gdx.graphics.getHeight()) {
        //     targetHeight = Gdx.graphics.getHeight();
        //     targetWidth = targetHeight * aspectRatio;
        // }
        // menuImage.setSize(targetWidth, targetHeight);
        // menuImage.setPosition((Gdx.graphics.getWidth() - targetWidth) / 2, (Gdx.graphics.getHeight() - targetHeight) / 2);


        // Tambahkan gambar ke stage
        stage.addActor(menuImage);

        // Atur input processor agar stage dapat menangani event (misalnya klik)
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Bersihkan layar
        Gdx.gl.glClearColor(0, 0, 0, 1); // Warna latar belakang hitam
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Gambar stage
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f)); // Batasi delta time untuk menghindari glitch pada FPS rendah
        stage.draw();

        // Contoh: Jika Anda ingin menambahkan logika interaksi, seperti klik untuk memulai game
        if (Gdx.input.justTouched()) {
            // Ganti ke layar game utama atau layar lain
            // game.setScreen(new GameScreen(game)); // Ganti GameScreen dengan kelas layar game Anda
            // dispose(); // Panggil dispose() untuk membersihkan sumber daya layar ini
            Gdx.app.log("MainMenuScreen", "Layar disentuh!");
        }
    }

    @Override
    public void resize(int width, int height) {
        // Perbarui viewport stage saat ukuran layar berubah
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // Tidak ada yang perlu dilakukan untuk layar menu sederhana
    }

    @Override
    public void resume() {
        // Tidak ada yang perlu dilakukan untuk layar menu sederhana
    }

    @Override
    public void hide() {
        // Dipanggil saat layar ini tidak lagi menjadi layar aktif
        // Bersihkan input processor
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        // Bersihkan sumber daya saat layar dibuang
        batch.dispose();
        mainMenuTexture.dispose();
        stage.dispose();
    }
}
