
package com.badlogic.gradletest;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class HelloApp extends ApplicationAdapter {

    public static final int RADIUS = 8;

    public static final int TYPE_BASIC = 0;
    public static final int TYPE_P = 1;

    public static final int SCREEN_HEIGHT = 480;
    public static final int SCREEN_WIDTH = 800;
    /**
     * The fraction of original speed/direction retained after collision.
     */
    public static final float CONFIG_RETAIN_ENERGY_FRACTION = 0.5f;

    /**
     * The size of newly generated speed after collision.
     */
    public static final float CONFIG_ENERGY_NEW = 0.66f;
    public static final float CONFIG_CRASH_DISTANCE = 1f;
    public static final int CONFIG_NUM_SPAWNED_PARTICLES = 3;
    // particle size
    public static final float P_SIZE = 0.5f;
    // target size
    public static final float SIZE_T = 1f;

    enum State {
        START,
        READY,
        GOING;
    }

    private SpriteBatch batch;
    private Texture img;
    private OrthographicCamera camera;
    private Rectangle bucket;
    private Sound dropSound;
    private Music rainMusic;
    private Model model;
    private ModelInstance instance;
    private ModelBatch modelBatch;
    private PerspectiveCamera cam;
    private Environment environment;
    private CameraInputController camController;
    private List<Thing> things = new ArrayList<Thing>();
    private Model sphereModel;
    private ModelInstance sphereInstance;
    private long frame;
    private Material thingMaterial;
    private Texture imgfg;
    private ShapeRenderer shapeRenderer;
    private Model projectileModel;

    private float flash = 0;
    private Vector3 swipe;
    private HelloApp.Thing firstParticle;
    private BitmapFont bitmapFont;
    private Texture imgReset;
    private Texture imgInfo;

    private long score;

    private State state = State.START;

    @Override
    public void create() {
        batch = new SpriteBatch();
        modelBatch = new ModelBatch();
        shapeRenderer = new ShapeRenderer();
        img = new Texture("background.png");
        imgfg = new Texture("foreground.png");
        imgReset = new Texture("reset.png");
        imgInfo = new Texture("info.png");

        bitmapFont = new BitmapFont();

//		try {
//			new FreeTypeFontGenerator(Gdx.files.internal("test.fnt"));
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		Bullet.init();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        cam.position.set(10f, 10f, 10f);
        cam.position.set(0f, 0f, 15f);
        cam.lookAt(0, 0, 0);
        cam.near = 0.1f;
        cam.far = 300f;
        cam.update();

        bucket = new Rectangle();
        bucket.x = SCREEN_WIDTH / 2 - 64 / 2;
        bucket.y = 20;
        bucket.width = 64;
        bucket.height = 64;

//        Thing e1 = new Thing();
//        things.add(e1);
//        e1.quat.set(new Vector3(1, 0, 0), 1);
//        e1.quatv.set(new Vector3(1, 1, 1), 1f);

//        Thing e = new Thing();
//        e.quatv.set(new Vector3(0, 0, 5), 1);
//        things.add(e);

//        Thing e3 = new Thing();
//        e3.quatv.set(new Vector3(1, 0.1f, 0), 1);
//        things.add(e3);

//        Thing e4 = new Thing();
//        e4.setDirection(new Vector3(0, 0, 1));
//        Vector3 direction = new Vector3(0, 0, 1);
//        e4.getDirection(direction);
//        things.add(e4);

        // load the drop sound effect and the rain background "music"
        dropSound = Gdx.audio.newSound(Gdx.files.internal("clap.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("noise.wav")); // can be mp3

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        ModelBuilder modelBuilder = new ModelBuilder();

        thingMaterial = new Material(ColorAttribute.createDiffuse(Color.GREEN));

        model = modelBuilder.createBox(SIZE_T, SIZE_T, SIZE_T,
                thingMaterial,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        instance = new ModelInstance(model);

        sphereModel = modelBuilder.createSphere(3f, 3f, 3f, 20, 20,
//                new Material(ColorAttribute.createDiffuse(Color.ORANGE)),
                new Material(ColorAttribute.createDiffuse(1, 1, 0, 0.5f)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        sphereInstance = new ModelInstance(sphereModel);


        projectileModel = modelBuilder.createSphere(P_SIZE, P_SIZE, P_SIZE, 5, 5,
                new Material(ColorAttribute.createDiffuse(Color.RED)),
//                new Material(ColorAttribute.createDiffuse(1, 1, 0, 0.1f)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);


        // start the playback of the background music immediately
        rainMusic.setLooping(true);
        rainMusic.play();

        camController = new CameraInputController(cam);
//        camController

        Gdx.input.setInputProcessor(new InputMultiplexer(
                camController,

                new InputAdapter() {
                    @Override
                    public boolean keyDown(int keycode) {
//                        dropSound.play();
                        return super.keyDown(keycode);
                    }

                    @Override
                    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                        super.touchDown(screenX, screenY, pointer, button);
                        if (isInState(State.READY)) {

                            swipe = new Vector3(screenX, screenY, 0);

                            Vector3 pos1 = new Vector3(0, 0, 1);
                            pos1.prj(cam.view.cpy().inv());
//                        pos1.prj(cam.view);

                            firstParticle = shootP(pos1, new Vector3());
                        }
                        return true;
                    }

                    @Override
                    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                        super.touchUp(screenX, screenY, pointer, button);
                        if (isInState(State.START)) {
                            state = State.READY;
                        } else if (isInState(State.READY)) {

                            swipe.sub(screenX, screenY, 0);
                            swipe.x *= -1;
                            swipe.prj(cam.view.cpy().inv());
//                        cam.unproject(swipe);
                            swipe.nor();
//                        shootP(getRandomVector(), getRandomVector());
                            firstParticle.setDirection(swipe);

//                        float pan = (2f * screenX / Gdx.graphics.getWidth()) - 1;
//                        Gdx.app.log("touch ", "pan " + pan);
//                        dropSound.play((float) screenY / Gdx.graphics.getHeight(), 1, pan);
                            playHitSound(firstParticle.pos, cam);

                            state = State.GOING;

                        } else if (isInState(State.GOING)) {
                            Vector3 touchPos = new Vector3();
                            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                            camera.unproject(touchPos);
                            if (SCREEN_WIDTH - 64 < touchPos.x && SCREEN_HEIGHT - 64 < touchPos.y) {
                                reset();
                                state = State.READY;
                            }
                        }
//                        shootP(new Vector3(0, 0, 1), swipe);
                        return true;
                    }
                }));


        reset();

    }

    private void reset() {
        score = 0;
        things.clear();
        for (int i = 0; i < 10; i++) {
            Thing t = new Thing(TYPE_BASIC);
//            t.setDirection(randomize(new Vector3()));
            randomize(t.pos, 1f);
            t.adjustToSphere();
            things.add(t);
        }
    }

    private Vector3 getRandomVector() {
        return getRandomVector(1f);
    }

    private Vector3 getRandomVector(float m) {
        return randomize(new Vector3(), m);
    }

    @Override
    public void render() {

        frame++;
//        frame = 1;

        camController.update();

        camera.update();

        Gdx.gl.glEnable(GL10.GL_BLEND);
        Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        Gdx.gl.glClearColor(0.5f * flash, 0.7f + 0.5f * flash, 0.6f + 0.5f * flash, 0.0f);
//		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT
//                | GL10.GL_ALPHA_BITS
        );

        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - 64 / 2;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();

        if (bucket.x < 0) bucket.x = 0;
        if (bucket.x > SCREEN_WIDTH - 64) bucket.x = SCREEN_WIDTH - 64;

        drawBackground();

        batch.setProjectionMatrix(camera.combined);
//        batch.begin();
//        batch.draw(img, bucket.x, bucket.y);
//        batch.end();

        updateThings();

        checkCollisions1();

        checkCollisionsP();

//        modelBatch.begin(cam);

//        instance.transform.idt();
//        modelBatch.render(instance, environment);
//
//        instance.transform.setToTranslation(1, 0, 0);
//        modelBatch.render(instance, environment);
//
//        instance.transform.setToTranslation(0, 0, 1);
//        modelBatch.render(instance, environment);
//
//        instance.transform.setToTranslation(2, 0, 0);
//        modelBatch.render(instance, environment);
//
//        modelBatch.end();

//        instance.materials.

        drawThings();

        drawCore();

        drawForeground();

//        drawShapes();

//        drawFlashes(flash);

        flash *= 0.95f;

    }

    private void updateThings() {
        for (Iterator<Thing> iterator = things.iterator(); iterator.hasNext(); ) {
            Thing thing = iterator.next();
            if (thing.dead) {
                iterator.remove();
            } else {
                thing.update();
            }
        }
    }

    private void checkCollisions1() {
        for (int i = 0; i < things.size(); i++) {
            Thing thing1 = things.get(i);
            for (int j = i + 1; j < things.size(); j++) {
                Thing thing2 = things.get(j);
//                collision(thing1, thing2);
            }
        }
    }

    private void checkCollisionsP() {
        final List<Thing> snapshot = new ArrayList<Thing>(things);

        for (Thing thingP : snapshot) {

            if (thingP.type == TYPE_P) {
                for (Thing thingT : snapshot) {
                    if (thingT.type == TYPE_BASIC) {
                        collisionP(thingP, thingT);
                    }
                }
            }

        }
    }

    private void drawThings() {
        modelBatch.begin(cam);

        for (Thing thing : things) {
//            if (frame % 60 == 0) {
//                thing.modelInstance.materials.get(0).set(ColorAttribute.createDiffuse(new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), MathUtils.random())));
//            }
            thing.getTransform(thing.modelInstance.transform);
            modelBatch.render(thing.modelInstance, environment);
        }

        modelBatch.end();
    }

    private void drawCore() {
        //        if (frame % 2 == 1)
        {
            Gdx.gl.glEnable(GL10.GL_BLEND);
            modelBatch.begin(cam);
            sphereInstance.transform.setToScaling(2, 2, 2);
            modelBatch.render(sphereInstance, environment);
            modelBatch.end();
            Gdx.gl.glDisable(GL10.GL_BLEND);
        }
    }

    private void drawBackground() {
        batch.begin();
//        batch.draw(img, 0, 0);
        batch.draw(img, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        batch.end();
    }

    private void drawForeground() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
//        batch.
//        batch.draw(imgfg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(imgfg, 0, 0, SCREEN_WIDTH, SCREEN_WIDTH);
        if (isInState(State.GOING)) {
            batch.draw(imgReset, SCREEN_WIDTH - 64, SCREEN_HEIGHT - 64, 64, 64);
            drawScore();
        }
        if (isInState(State.START)) {
            batch.draw(imgInfo, 0, 0, SCREEN_WIDTH, SCREEN_WIDTH);
        }
        batch.end();
    }

    private boolean isInState(State... states) {
        for (State s : states) {
            if (s == state) {
                return true;
            }
        }
        return false;
    }

    private void drawScore() {
        bitmapFont.draw(batch, "particles: " + score, 30, 450);
    }

    private void drawShapes() {
        float x = 5;
        float y = 5;
        float x2 = 10;
        float y2 = 10;
        float radius = 3;
        float width = 3;
        float height = 3;


//            shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(cam.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 0, 1);

        shapeRenderer.line(x, y, x2, y2);
        shapeRenderer.line(x, y, 1, x2, y2, 1);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.circle(x, y, radius);
        shapeRenderer.end();
    }

    private void drawFlashes(float flash) {
        float x = 5;
        float y = 5;
        float x2 = 10;
        float y2 = 10;
        float radius = 3;
        float width = 100;
        float height = 100;


//            shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(flash, 1, 0, flash);

//        shapeRenderer.line(x, y, x2, y2);
//        shapeRenderer.line(x, y, 1, x2, y2, 1);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.circle(x, y, radius);
        shapeRenderer.end();
    }

    private void collision(Thing thing1, Thing thing2) {
        if (thing1 == thing2) {
            return;
        }
        if (thing1.pos.dst2(thing2.pos) < 1) {
            Vector3 pos = new Vector3(thing1.pos);
//            cam.project(pos);
//            pos.prj(cam.combined);
            pos.prj(cam.view);
//            float pan = (2f * pos.x / Gdx.graphics.getWidth()) - 1;
            float volume = (float) Math.exp(0.1f * pos.z);
            Gdx.app.log("touch ", "collision " + pos + " vol " + volume);
            dropSound.play(volume, 1, pos.x);
            thing1.setDirection(getRandomVector());
            thing2.setDirection(getRandomVector());

        }
    }

    /**
     * @param thingP projectile
     * @param thingT target - what is being hit
     */
    private void collisionP(Thing thingP, Thing thingT) {
        assert thingP.type == TYPE_P;
        assert thingT.type == TYPE_BASIC;
        if (thingT.dead) {
//  continue - lets have bigger bonus
// return;
        }
        if (thingP.pos.dst2(thingT.pos) < CONFIG_CRASH_DISTANCE * CONFIG_CRASH_DISTANCE) {

            playHitSound(thingT.pos, cam);

            thingP.dead = true;
            thingT.dead = true;

            flash += 1f;

            Vector3 direction = thingP.getDirection(new Vector3());
            direction.scl(CONFIG_RETAIN_ENERGY_FRACTION);

//          retained energy:
//           shootP(thingt.pos, direction);

            for (int i = 0; i < CONFIG_NUM_SPAWNED_PARTICLES; i++) {
                shootP(thingT.pos, getRandomVector(CONFIG_ENERGY_NEW).add(direction));
                score++;
            }

        }
    }

    private Thing shootP(Vector3 from, Vector3 direction) {
        Thing t = new Thing(TYPE_P);
//            t.setDirection(randomize(new Vector3()));
        t.pos.set(from);
        t.adjustToSphere();
        t.setDirection(direction);
        things.add(t);
        return t;
    }

    /**
     * Plays sound in world depending on where camera is
     *
     * @param worldPos
     * @param povCam
     */
    private void playHitSound(Vector3 worldPos, PerspectiveCamera povCam) {

        Vector3 camPos = new Vector3(worldPos);
//            cam.project(pos);
//            pos.prj(cam.combined);
        // position in camera coordinates
        camPos.prj(povCam.view);
//            float pan = (2f * pos.x / Gdx.graphics.getWidth()) - 1;
        float volume = (float) Math.exp(0.1f * camPos.z);
        Gdx.app.log("touch ", "collision " + camPos + " vol " + volume);
        dropSound.play(volume, 1, camPos.x);
    }

    private Vector3 randomize(Vector3 random, float m) {
        return random.set(MathUtils.random(-m, m), MathUtils.random(-m, m), MathUtils.random(-m, m));
    }

    class Thing {
        final Vector3 pos = new Vector3(5, 0, 0);
        final Quaternion quat = new Quaternion();
        final Quaternion quatv = new Quaternion();

        final int type;
        boolean dead = false;

        ModelInstance modelInstance;

        Thing(int type) {
            this.type = type;
            switch (type) {
                case TYPE_BASIC:
                    modelInstance = new ModelInstance(model);
                    break;
                case TYPE_P:
                    modelInstance = new ModelInstance(projectileModel);
                    break;
            }
        }

        public void update() {
            Quaternion q = new Quaternion();
            q.slerp(quatv, 1f);
            quat.mul(q);
            pos.mul(q);
        }

        private void getTransform(Matrix4 transform) {
            transform.idt();
//            transform.translate(5, 0, 0);
            transform.setTranslation(pos);
//            transform.setTranslation(pos) ;
//            transform.translate(pos);
            transform.rotate(quat);
//            Vector3 vector3 = new Vector3();
//            thing.quat.getAxisAngle(vector3);

        }

        /**
         * Sets absolute direction of movement
         *
         * @param direction
         */
        public void setDirection(Vector3 direction) {
            Vector3 axis = new Vector3(pos);
            axis.crs(direction);
            float speed = direction.len();
            quatv.set(axis, speed);
        }

        /**
         * Absolute direction of movement
         *
         * @param direction
         */
        public Vector3 getDirection(Vector3 direction) {
            // predict move to find the direction
            direction.set(pos).mul(quatv).sub(pos);
            // apply correction from length per step to angular speed in degrees per step
            direction.scl(180f / (MathUtils.PI * RADIUS));
            return direction;
        }


        void adjustToSphere() {
            pos.nor().scl(RADIUS);
        }
    }
}

