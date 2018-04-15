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
        cfg.x = 1920;
        cfg.y = -100;

        LibGdxShower shower = new LibGdxShower();
        new LwjglApplication(shower, cfg);
        MyStrategy.activePainter = new LibGdxPainter(shower);
    }

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private LibGdxDataToPaint libGdxObjs = new LibGdxDataToPaint();
    private OrthographicCamera camera;

    private boolean didDrawPP; //TODO
    private boolean resized;


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

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        drawPP(data);

        shapes.begin(ShapeRenderer.ShapeType.Filled);

        for (LibGdxObj obj : data.objs) {
            shapes.setColor(obj.getColor());
            shapes.circle(obj.getFX(), obj.getFY(), obj.getFR());
            if (obj.type == UnitType.PLAYER) {

                Point2D speedPoint = obj.getSpeedVector().mul(50).add(obj.getPos());

                if (obj.isMy) {
                    //speed vector
                    shapes.setColor(Color.GREEN);
                    shapes.line(obj.getFX(), obj.getFY(), speedPoint.getFX(), speedPoint.getFY());

                    //move vector
                    shapes.setColor(Color.RED);
                    shapes.line(obj.getFX(), obj.getFY(), data.move.getFX(), data.move.getFY());

                    //move vector
                    shapes.setColor(Color.BLACK);
                    shapes.line(obj.getFX(), obj.getFY(), data.move.wantedTargetPos.getFX(), data.move.wantedTargetPos.getFY());
                }
                Point2D visionCenter = obj.getVisionCenter();
              /*  if (true) {
                    continue;
                }*/

                float vision = (float) obj.getVisionDistance();

                if (vision > 0) {

                    shapes.circle(visionCenter.getFX(), visionCenter.getFY(), 4);


                    shapes.end();
                    shapes.begin(ShapeRenderer.ShapeType.Line);


                    shapes.circle(visionCenter.getFX(), visionCenter.getFY(), vision);

                    shapes.end();
                    shapes.begin(ShapeRenderer.ShapeType.Filled);
                }
            }
        }
        shapes.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        for (LibGdxObj obj : data.objs) {
            if (obj.wantPrintMass()) {
                font.setColor(Color.BLACK);
                font.draw(batch, Utils.format(obj.mass), obj.getFX() - 10, obj.getFY() - 5);
            }
        }
        batch.end();

        if (!resized) {
            resized = true;
            Gdx.graphics.setWindowedMode(700, 700);
        }
    }

    private void drawPP(LibGdxDataToPaint data) {

        int cX = 0;
        int cY = 0;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (data.potentialMap != null) {
            cX = (int) ((Gdx.input.getX() * (990 / 700f)) / data.potentialMap.cellSize);
            cY = (int) ((Gdx.input.getY() * (990 / 700f)) / data.potentialMap.cellSize);

            PlainArray plainArray = data.potentialMap.map;

            didDrawPP = true;

            int cellSize = data.potentialMap.cellSize;

            int cellsX = Main.game.GAME_WIDTH / cellSize;
            int cellsY = Main.game.GAME_HEIGHT / cellSize;

            double max = plainArray.getMax();
            double min = plainArray.getMin();
            double delta = max - min;
            if (delta == 0) {
                delta = 1;
            }

            double root = root(delta, delta);

            int maxDistance = 32;
            double squareMaxDistance = Math.pow(cellSize * maxDistance, 2);


            //  mys.log("start draw " + myGroup.vehicleType);
            for (int x = 0; x < cellsX; x++) {
                for (int y = 0; y < cellsY; y++) {
                    double v = plainArray.get(x, y) - min;

                    int alpha = (int) (((Math.pow(root, v)) / delta) * 220);
                    //int alpha = (int) ((v / delta) * 220);
                    if (alpha > 0) {
                        int realX = x * cellSize;
                        int realY = y * cellSize;
                        int centerX = realX + cellSize / 2;
                        int centerY = realY + cellSize / 2;

                        /*if (RESTRICTED_PP_DRAW && myGroup.getAveragePoint().squareDistance(centerX, centerY) > squareMaxDistance) {
                            continue; // too far for decide
                        }*/
                        //mys.log(String.format("%s %s %s - v: %s", myGroup.vehicleType, realX, realY, (int) v));
                        //System.out.print(String.format("%s %s %s - v: %s", myGroup.vehicleType, realX, realY, (int) v));
                        //    System.out.print((int) v + " ");
                        shapes.setColor(new Color((133 / 255f), alpha / 255f, (255 - alpha) / 255f, (100 / 255f)));
                        //shapes.setColor(Color.GOLD);
                        shapes.rect(realX, realY, cellSize, cellSize);
                    }
                }
            }

            Point2D calcCenter = data.potentialMap.mainUnitPosPotential;
            float realCalcDistance = (float) data.potentialMap.calcDistancePotential * cellSize;
            float realX = (float) calcCenter.getX() * cellSize;
            float realY = (float) calcCenter.getY() * cellSize;

            shapes.setColor(Color.GOLD);
            shapes.circle(realX, realY, (cellSize / 2));

            shapes.setColor(Color.CYAN);
            //shapes.setColor(Color.GOLD);
            shapes.rect(cX * cellSize, cY * cellSize, cellSize, cellSize);

            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Line);


            shapes.circle(realX, realY, realCalcDistance);

            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Filled);
        }

        shapes.end();

        batch.begin();

        if (data.potentialMap != null) {
            font.setColor(Color.BLACK);
            int realX = cX * data.potentialMap.cellSize;
            int realY = cY * data.potentialMap.cellSize;

            String msg = Utils.format(data.potentialMap.map.get(cX + 10, cY));
            if (data.mainUnit != null) {
                Point2D vectorToCursor = new Point2D(cX, cY).sub(data.mainUnit.getPotentialPos());
                float angle = vectorToCursor.angle();
                msg += " v: x" + Utils.format(vectorToCursor.getX()) + " y" + Utils.format(vectorToCursor.getY()) + " a: " + Utils.format(angle);
            }


            font.draw(batch, msg, realX, realY);
        }

        batch.end();

    }

    public static double root(double num, double root) {
        return Math.pow(Math.E, Math.log(num) / root);
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