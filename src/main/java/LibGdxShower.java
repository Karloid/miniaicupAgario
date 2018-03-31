import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class LibGdxShower implements ApplicationListener {

    static {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.foregroundFPS = 60;
        cfg.title = "hello-world";
        cfg.useGL30 = false;
        cfg.width = Main.game.GAME_WIDTH;
        cfg.height = Main.game.GAME_HEIGHT;

        LibGdxShower shower = new LibGdxShower();
        new LwjglApplication(shower, cfg);
        MyStrategy.activePainter = new LibGdxPainter(shower);
    }

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private LibGdxDataToPaint libGdxObjs = new LibGdxDataToPaint();
    private OrthographicCamera camera;


    @Override
    public void create() {
        // Create a full-screen camera:
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
// Set it to an orthographic projection with "y down" (the first boolean parameter)
        camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);
        shapes = new ShapeRenderer();
        shapes.setProjectionMatrix(camera.combined);
        font = new BitmapFont(true);
        font.setColor(Color.RED);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
    }

    @Override
    public void render() {
        LibGdxDataToPaint data = this.libGdxObjs;
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        shapes.begin(ShapeRenderer.ShapeType.Filled);

        for (LibGdxObj obj : data.objs) {
            shapes.setColor(obj.getColor());
            shapes.circle(obj.getFX(), obj.getFY(), obj.getFR());
            if (obj.type == UnitType.PLAYER) {
                Point2D p = obj.getVisionCenter();

                //speed vector
                shapes.setColor(Color.CYAN);
                shapes.line(obj.getFX(), obj.getFY(), p.getFX(), p.getFY());

                float vision = obj.getVisionDistance();
                if (vision > 0) {


                    shapes.circle(p.getFX(), p.getFY(), 4);


                    shapes.end();
                    shapes.begin(ShapeRenderer.ShapeType.Line);


                    shapes.circle(p.getFX(), p.getFY(), vision);

                    shapes.end();
                    shapes.begin(ShapeRenderer.ShapeType.Filled);
                }
            }

        }

        shapes.end();

        batch.begin();
        for (LibGdxObj obj : data.objs) {
            if (obj.wantPrintMass()) {
                font.setColor(Color.BLACK);
                font.draw(batch, Utils.format(obj.mass), obj.getFX() - 10, obj.getFY() - 5);
            }
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    public void setObjects(LibGdxDataToPaint libGdxObjs) {
        this.libGdxObjs = libGdxObjs;
    }

}