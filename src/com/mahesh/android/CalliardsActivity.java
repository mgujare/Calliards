package com.mahesh.android;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.shape.Shape;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.batch.SpriteBatch;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.time.TimeConstants;

import android.content.Intent;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class CalliardsActivity extends SimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener, IOnAreaTouchListener, IOnMenuItemClickListener  {
	// ===========================================================
		// Constants
		// ===========================================================

		private static final int CAMERA_WIDTH = 360;
		private static final int CAMERA_HEIGHT = 240;
		
		private final float MAX_DISTANCE_FLING = 40f;
	    private final float MAX_VELOCITY_CONST = 250f;
	    private final float DEFAULT_VELOCITY = 50f;
	    
	   
	    protected static final int MENU_RESET = 0;
		protected static final int MENU_QUIT = MENU_RESET + 1;
		protected static final int MENU_PAUSE = MENU_RESET + 2;

		// ===========================================================
		// Fields
		// ===========================================================
	    
	    protected Camera mCamera;
	    
        private static Sprite face;
	    private static Body body;
	    
	    private static CopyOnWriteArrayList<Shape> coinSprites = new CopyOnWriteArrayList<Shape>();

		private BitmapTextureAtlas mBitmapTextureAtlas;
		
		private BitmapTexture mBitmapTexture;

		private ITextureRegion mBoxFaceTextureRegion;
		private ITextureRegion mCoin1TextureRegion;
		private ITextureRegion mCoin2TextureRegion;
		private ITextureRegion mCoin3TextureRegion;
		private ITextureRegion mCoin4TextureRegion;
		private ITextureRegion mCoin5TextureRegion;
		private ITextureRegion kCoinTextureRegion;
		private ArrayList<ITextureRegion> mCircleFaceTextureRegion = new ArrayList<ITextureRegion>();
		
		

		private int mFaceCount = 0;
		
		private int mCoinCount = 0;

		private PhysicsWorld mPhysicsWorld;

		private float mGravityX;
		private float mGravityY;

		private Scene mScene;
		
		protected MenuScene mMenuScene;
		private CameraScene mPauseScene;

		private BitmapTextureAtlas mMenuTexture;
		protected ITextureRegion mMenuResetTextureRegion;
		protected ITextureRegion mMenuQuitTextureRegion;
		protected ITextureRegion mPausedTextureRegion;

		// ===========================================================
		// Constructors
		// ===========================================================
		
		


		// ===========================================================
		// Getter & Setter
		// ===========================================================

		// ===========================================================
		// Methods for/from SuperClass/Interfaces
		// ===========================================================

		@Override
		public EngineOptions onCreateEngineOptions() {
			Toast.makeText(this, "Touch the screen to add objects. Touch an object to shoot it up into the air.", Toast.LENGTH_LONG).show();

			this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

			return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
		}

		@Override
		public void onCreateResources() {
			BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

			this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 32, 320, TextureOptions.BILINEAR); //3rd param should correspond to number of sprites.. 2nd param is width of sprite.
			this.mBoxFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "face_box.png", 0, 0); // 64x32
			this.mCoin1TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "face_circle_0.png", 0, 32); // 64x32
			this.mCoin2TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "face_circle_1.png", 0, 64); // 64x32
			this.mCoin3TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "face_circle_2.png", 0, 96); // 64x32
			this.mCoin4TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "face_circle_3.png", 0, 128); // 64x32
			
			this.kCoinTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "killer.png", 0, 160); // 64x32
			
			this.mBitmapTextureAtlas.load();
			
			this.mCircleFaceTextureRegion.add(mCoin1TextureRegion);
			this.mCircleFaceTextureRegion.add(mCoin2TextureRegion);
			this.mCircleFaceTextureRegion.add(mCoin3TextureRegion);
			this.mCircleFaceTextureRegion.add(mCoin4TextureRegion);
			
			this.mMenuTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 192, TextureOptions.BILINEAR);
			this.mMenuResetTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mMenuTexture, this, "menu_reset.png", 0, 0);
			this.mMenuQuitTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mMenuTexture, this, "menu_quit.png", 0, 50);
			this.mPausedTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mMenuTexture, this, "home.png", 0, 100);
			this.mMenuTexture.load();
		}

		@Override
		public Scene onCreateScene() {
			this.mEngine.registerUpdateHandler(new FPSLogger());
			
			this.createMenuScene();

			this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

			this.mScene = new Scene();
			this.mScene.setBackground(new Background(0, 0, 0));
			this.mScene.setOnSceneTouchListener(this);

			final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
			final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
			final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
			final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
			final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

			final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
			PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
			PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
			PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
			PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

			this.mScene.attachChild(ground);
			this.mScene.attachChild(roof);
			this.mScene.attachChild(left);
			this.mScene.attachChild(right);

			this.mScene.registerUpdateHandler(this.mPhysicsWorld);

			this.mScene.setOnAreaTouchListener(this);
			
			if(face!=null){
				this.mScene.detachChild(face);
				face.dispose();
				face = null;
			}
			
			if(coinSprites.size() > 0)
				coinSprites.removeAll(coinSprites);
				
			
			addCoins();
			
			/* The actual collision-checking. */
			this.mScene.registerUpdateHandler(new IUpdateHandler() {
				@Override
				public void reset() { }

				@Override
				public void onUpdate(final float pSecondsElapsed) {
					
					int index = 0;
					
					for(final Shape spriteA : coinSprites){
					
									if(spriteA.collidesWith(face)  ) {
											
											if(index==0){
														//Toast.makeText(CalliardsActivity.this, "Collision Detected with chld:", Toast.LENGTH_LONG).show();
														Log.i("Collision","Collision Detected:"+pSecondsElapsed);
														removeCoin((Sprite)spriteA);
														coinSprites.remove(spriteA);
														if(mCoinCount > 0)
															mCoinCount--;
														}
														else {
															//Add Killer coin.
															
															
															FixtureDef objectFixtureDef3 = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
															Sprite killer_coin = new Sprite(200, 200, kCoinTextureRegion, CalliardsActivity.this.getVertexBufferObjectManager());
															Body body = PhysicsFactory.createBoxBody(CalliardsActivity.this.mPhysicsWorld, killer_coin, BodyType.DynamicBody, objectFixtureDef3);
															CalliardsActivity.this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(killer_coin, body, true, true));
												
																//face.animate(new long[]{200,200}, 0, 1, true);
																killer_coin.setUserData(body);
																CalliardsActivity.this.mScene.registerTouchArea(killer_coin);
																CalliardsActivity.this.mScene.attachChild(killer_coin);
															
														}
														
													} 
													
										
									index++;
							
					}		
				}
			});
			
		

			return this.mScene;
		}
		

		@Override
		public boolean onAreaTouched( final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea,final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
			if(pSceneTouchEvent.isActionDown()) {
				
				/*
				final AnimatedSprite face = (AnimatedSprite) pTouchArea;
				
				Vector2 touchVector = new Vector2(pTouchAreaLocalX, pTouchAreaLocalY);

				//this.jumpFace(face,touchVector);
				
				float distance = getDistance(Xlocal, Ylocal, pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				long timeUp = System.currentTimeMillis();
				
				//shootBall(face,pSceneTouchEvent.getX(),pSceneTouchEvent.getY(),distance,(float) (timeUp - timeDown) / TimeConstants.MILLISECONDS_PER_SECOND);
				
				return true;
				*/
			}
				

			return false;
		}
		
		
		private float Xlocal = 0.0f;
	    private float Ylocal = 0.0f;
	    private long timeDown = 0;

		@Override
		public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
			if(this.mPhysicsWorld != null) {
				if(pSceneTouchEvent.isActionDown()) {
					this.addFace(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
					
					Xlocal = pSceneTouchEvent.getX();
		            Ylocal = pSceneTouchEvent.getY();   
					
					return true;
				}
				else if (pSceneTouchEvent.isActionUp()) {

		            float X = (pSceneTouchEvent.getX() - Xlocal);
		            float Y = (pSceneTouchEvent.getY() - Ylocal);

		            float distance = getDistance(Xlocal, Ylocal, pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
		            if( distance >= MAX_DISTANCE_FLING){
		                long timeUp = System.currentTimeMillis();

		                System.out.println("Time up is "+timeUp);
		                Log.i("Coin Count","c:"+this.mCoinCount+"sc:"+coinSprites.size());
		               // shootBall(face,X, Y, distance, (float) (timeUp - timeDown) / TimeConstants.MILLISECONDS_PER_SECOND);
		                shootBall(face,X, Y, distance, (float) (timeUp - timeDown) / TimeConstants.SECONDS_PER_MINUTE);
		            } 
			}
				
			}	
			
			return false;
		}

		@Override
		public void onAccelerationAccuracyChanged(final AccelerationData pAccelerationData) {

		}

		@Override
		public void onAccelerationChanged(final AccelerationData pAccelerationData) {
			
			this.mGravityX = pAccelerationData.getX();
			this.mGravityY = pAccelerationData.getY();

			final Vector2 gravity = Vector2Pool.obtain(this.mGravityX, this.mGravityY);
			this.mPhysicsWorld.setGravity(gravity);
			Vector2Pool.recycle(gravity);
			
		}

		@Override
		public void onResumeGame() {
			super.onResumeGame();

			this.enableAccelerationSensor(this);
			
		}

		@Override
		public void onPauseGame() {
			super.onPauseGame();

			this.disableAccelerationSensor();
		}

		@Override
		public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
			if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
				if(this.mScene.hasChildScene()) {
					/* Remove the menu and reset it. */
					this.mMenuScene.back();
				} else {
					/* Attach the menu. */
					this.mScene.setChildScene(this.mMenuScene, false, true, true);
				}
				return true;
			} else {
				return super.onKeyDown(pKeyCode, pEvent);
			}
		}

		@Override
		public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem, final float pMenuItemLocalX, final float pMenuItemLocalY) {
			switch(pMenuItem.getID()) {
				case MENU_RESET:
					/* Restart the animation. */
					this.mScene.reset();

					/* Remove the menu and reset it. */
					this.mScene.clearChildScene();
					this.mMenuScene.reset();
					return true;
				case MENU_QUIT:
					/* End Activity. */
					finish();
					return true;
				case MENU_PAUSE:
					/* Return to Main Activity */
					
					Intent intent = new Intent();
	                intent.setClass(CalliardsActivity.this, MainActivity.class);
	                startActivity(intent);
					
					
					return true;
					
				default:
					return false;
			}
		}
		
		// ===========================================================
		// Methods
		// ===========================================================
		
		
		protected void createMenuScene() {
			this.mMenuScene = new MenuScene(this.mCamera);

			final SpriteMenuItem resetMenuItem = new SpriteMenuItem(MENU_RESET, this.mMenuResetTextureRegion, this.getVertexBufferObjectManager());
			resetMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
			this.mMenuScene.addMenuItem(resetMenuItem);

			final SpriteMenuItem quitMenuItem = new SpriteMenuItem(MENU_QUIT, this.mMenuQuitTextureRegion, this.getVertexBufferObjectManager());
			quitMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
			this.mMenuScene.addMenuItem(quitMenuItem);
			
			final SpriteMenuItem pauseMenuItem = new SpriteMenuItem(MENU_PAUSE, this.mPausedTextureRegion, this.getVertexBufferObjectManager());
			quitMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
			this.mMenuScene.addMenuItem(pauseMenuItem);

			this.mMenuScene.buildAnimations();

			this.mMenuScene.setBackgroundEnabled(false);

			this.mMenuScene.setOnMenuItemClickListener(this);
		}

		private void addFace(final float pX, final float pY) {
			this.mFaceCount++;

			

			final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
			
			

			if(this.mFaceCount <= 1){
				face = new Sprite(pX, pY, this.mBoxFaceTextureRegion, this.getVertexBufferObjectManager());
				body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
				this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));

				//face.animate(new long[]{200,200}, 0, 1, true);
				face.setUserData(body);
				this.mScene.registerTouchArea(face);
				this.mScene.attachChild(face);
			} 

			
		}
		
		private void addCoins() {
			
			 FixtureDef objectFixtureDef2 = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
			 
			
			
					for(int i=0;i<4;i++){
					
						this.mCoinCount++;
						
						 float pX = (float )Math.random() * (250 - 20) + 20;
							
						 float pY = (float )Math.random() * (300 - 20) + 20;
							
						  pX = CAMERA_WIDTH/2; pY = CAMERA_HEIGHT/2;
							
						  Log.i("Coords:"," x:"+pX+" y:"+pY +mCircleFaceTextureRegion.size());
						 
						  Sprite coin_face = new Sprite(pX, pY, this.mCircleFaceTextureRegion.get(i), this.getVertexBufferObjectManager());
						  Body body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, coin_face, BodyType.DynamicBody, objectFixtureDef2);
						  this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(coin_face, body, true, true));
			
							//face.animate(new long[]{200,200}, 0, 1, true);
							coin_face.setUserData(body);
							this.mScene.registerTouchArea(coin_face);
							this.mScene.attachChild(coin_face);
							
							coinSprites.add(coin_face);
						
					}	
			
			
		}
		
		private boolean removeCoin(Sprite coin){
			
			if(coin == null) {
				return false;
			}
			
			this.mScene.detachChild(coin);
			coin.dispose();
			this.mPhysicsWorld.destroyBody((Body)coin.getUserData());	// Remove from Physics world.
			coin = null;
			
			
			Log.i("Count of bodies:",""+this.mPhysicsWorld.getBodyCount());
			
			return true;
			
		}

		private void jumpFace(final AnimatedSprite face,Vector2 newPos) {
			
			/*
			final Body faceBody = (Body)face.getUserData();

			final Vector2 velocity = Vector2Pool.obtain(this.mGravityX * -50, this.mGravityY * -50);
			faceBody.setLinearVelocity(velocity);
			Vector2Pool.recycle(velocity);
			*/
			
			final Body faceBody = (Body)face.getUserData();
			
			Vector2 curPos = faceBody.getWorldCenter();
			Vector2 diff = newPos.sub(curPos);

			final float theta = (float)(diff.len() + Math.PI/2);
			final float r = 1;
			final float deltax = (float) (r*Math.cos(theta));
			final float deltay = (float) (r*Math.sin(theta));
			final Vector2 velocity = Vector2Pool.obtain(deltax * 20, deltay * 20);
			faceBody.setLinearVelocity(velocity);
			Vector2Pool.recycle(velocity);
			
		}
		
		private void shootBall(final Sprite face,final float pX, final float pY, final float pDistance, final float pTime) {

	        System.out.println("Time Final seconds "+pTime);
	        
	        final Body faceBody = (Body)face.getUserData();

	        float angleRad =(float)Math.atan2(pY, pX);
	        float velocity =(float) ((float) this.getVelocity(pTime) * 0.3);//(pDistance * 12.5f) / 100f;      
	        if(faceBody != null){           
	            float Vx = velocity * (float)Math.cos(angleRad);
	            float Vy = velocity * (float)Math.sin(angleRad);
	            faceBody.applyLinearImpulse(new Vector2(Vx,Vy), faceBody.getWorldCenter()); 
	            faceBody.setAngularDamping(0.8f); //to decrease velocity slowly. no linear no floaty :)
	            faceBody.setLinearDamping(0.5f);
	            faceBody.applyTorque(100f);
	        }
		}    
		
			private float getDistance(float x1, float y1, float x2, float y2){
		        float X2_ = (float)Math.pow(x2 - x1, 2);
		        float Y2_ = (float)Math.pow(y2 -  y1, 2);       
	
		        float distance = (float)Math.sqrt(X2_ + Y2_);       
		        return distance;
		    }
		
	        
	        private float getVelocity(float pTime) {
	            float velocity = MAX_VELOCITY_CONST - (pTime * 100f);
	            if (velocity < DEFAULT_VELOCITY) {
	                velocity = DEFAULT_VELOCITY;
	            }
	            System.out.println("velocity  "+velocity);
	            return velocity;
	        }
 

		// ===========================================================
		// Inner and Anonymous Classes
		// ===========================================================
}