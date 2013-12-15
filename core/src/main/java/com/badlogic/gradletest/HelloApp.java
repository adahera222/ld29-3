
package com.badlogic.gradletest;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
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

    @Override
    public void create() {
        batch = new SpriteBatch();
        modelBatch = new ModelBatch();
        shapeRenderer = new ShapeRenderer();
        img = new Texture("background.png");
        imgfg = new Texture("foreground.png");
//		try {
//			new FreeTypeFontGenerator(Gdx.files.internal("test.fnt"));
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		Bullet.init();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f, 10f, 10f);
        cam.lookAt(0, 0, 0);
        cam.near = 0.1f;
        cam.far = 300f;
        cam.update();

        bucket = new Rectangle();
        bucket.x = 800 / 2 - 64 / 2;
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

        for (int i = 0; i < 10; i++) {
            Thing t = new Thing();
//            t.setDirection(randomize(new Vector3()));
            randomize(t.pos);
            t.adjustToSphere();
            things.add(t);
        }

        // load the drop sound effect and the rain background "music"
        dropSound = Gdx.audio.newSound(Gdx.files.internal("clap.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("noise.wav")); // can be mp3

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        ModelBuilder modelBuilder = new ModelBuilder();

        thingMaterial = new Material(ColorAttribute.createDiffuse(Color.GREEN));

        model = modelBuilder.createBox(1f, 1f, 1f,
                thingMaterial,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        instance = new ModelInstance(model);

        sphereModel = modelBuilder.createSphere(3f, 3f, 3f, 20, 20,
//                new Material(ColorAttribute.createDiffuse(Color.ORANGE)),
                new Material(ColorAttribute.createDiffuse(1, 1, 0, 0.1f)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        sphereInstance = new ModelInstance(sphereModel);

        // start the playback of the background music immediately
        rainMusic.setLooping(true);
        rainMusic.play();

        camController = new CameraInputController(cam);

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
                        float pan = (2f * screenX / Gdx.graphics.getWidth()) - 1;
                        Gdx.app.log("touch ", "pan " + pan);
                        dropSound.play((float) screenY / Gdx.graphics.getHeight(), 1, pan);
                        return super.touchDown(screenX, screenY, pointer, button);
                    }
                }));

        shootP(getRandomVector());
    }

    private Vector3 getRandomVector() {
        return randomize(new Vector3());
    }

    @Override
    public void render() {

        frame++;
//        frame = 1;

        camController.update();

        camera.update();

        Gdx.gl.glEnable(GL10.GL_BLEND);
        Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        Gdx.gl.glClearColor(0.5f, 0, 0.5f, 0.0f);
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
        if (bucket.x > 800 - 64) bucket.x = 800 - 64;

        drawBackground();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(img, bucket.x, bucket.y);
        batch.end();

        updateThings();

        checkCollicions1();

        checkCollisionsP();

        modelBatch.begin(cam);

        instance.transform.idt();
        modelBatch.render(instance, environment);

        instance.transform.setToTranslation(1, 0, 0);
        modelBatch.render(instance, environment);

        instance.transform.setToTranslation(0, 0, 1);
        modelBatch.render(instance, environment);

        instance.transform.setToTranslation(2, 0, 0);
        modelBatch.render(instance, environment);

        modelBatch.end();

//        instance.materials.

        drawThings();

        drawCore();

        drawForeground();

        drawShapes();
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

    private void checkCollicions1() {
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

        for (Thing thingp : snapshot) {

            if (thingp.type == TYPE_P) {
                for (Thing thingt : snapshot) {
                    if (thingt.type == TYPE_BASIC) {
                        collisionP(thingp, thingt);
                    }
                }
            }

        }
    }

    private void drawThings() {
        modelBatch.begin(cam);

        for (Thing thing : things) {
            if (frame % 60 == 0) {
                instance.materials.get(0).set(ColorAttribute.createDiffuse(new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), MathUtils.random())));
            }
            thing.getTransform(instance.transform);
            modelBatch.render(instance, environment);
        }

        modelBatch.end();
    }

    private void drawCore() {
        //        if (frame % 2 == 1)
        {
            modelBatch.begin(cam);
            sphereInstance.transform.setToScaling(2, 2, 2);
            modelBatch.render(sphereInstance, environment);
            modelBatch.end();
        }
    }

    private void drawBackground() {
        batch.begin();
//        batch.draw(img, 0, 0);
        batch.draw(img, 0, 0, 800, 480);
        batch.end();
    }

    private void drawForeground() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
//        batch.
//        batch.draw(imgfg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(imgfg, 0, 0, 800, 480);
        batch.end();
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
     * @param thingp projectile
     * @param thingt target - what is being hit
     */
    private void collisionP(Thing thingp, Thing thingt) {
        assert thingp.type == TYPE_P;
        assert thingt.type == TYPE_BASIC;
        if (thingp.pos.dst2(thingt.pos) < 1) {

            playHitSound(thingt.pos, cam);

            thingp.dead = true;
            thingt.dead = true;

            for (int i = 0; i < 3; i++) {
                shootP(thingt.pos);
            }

        }
    }

    private void shootP(Vector3 from) {
        Thing t = new Thing();
        t.type = TYPE_P;
//            t.setDirection(randomize(new Vector3()));
        t.pos.set(from);
        t.adjustToSphere();
        t.setDirection(getRandomVector());
        things.add(t);
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

    private Vector3 randomize(Vector3 random) {
        return random.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f));
    }

    class Thing {
        final Vector3 pos = new Vector3(5, 0, 0);
        final Quaternion quat = new Quaternion();
        final Quaternion quatv = new Quaternion();

        int type = 0;
        boolean dead = false;

        Thing() {
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
        public void getDirection(Vector3 direction) {
            // predict move to find the direction
            direction.set(pos).mul(quatv).sub(pos);
        }


        void adjustToSphere() {
            pos.nor().scl(RADIUS);
        }
    }
}

