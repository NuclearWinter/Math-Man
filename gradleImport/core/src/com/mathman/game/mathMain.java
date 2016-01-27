package com.mathman.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class mathMain implements ApplicationListener, InputProcessor {
	public static final String TAG = "MathMan"; //Tag for console
    private SpriteBatch batch; //Batch for drawing
    private int currentFrame = 1; //Current frame in set for drawing the characters
    private String currentAtlasKey = new String("0001"); //the actual set for the atlas
    private TextureAtlas textureAtlas; //Math Man
    private Sprite mathMan; //Sprite character for Math Man (holds the character drawing)
    private TextureAtlas textureMathHero; //Math Hero
    private Sprite mathHero; //Sprite character for Math Hero
    private Texture background; //Background
    private float playerX = 120; //Players start X position (in this format so that it can be edited)
    private float playerY = 100; //Players start Y position 
    private float mathHeroX = 2000; //Math Hero's X position (starts here so that it is not seen)
    private long deltaCheck = System.currentTimeMillis(); //For keeping 60 FPS
    private int correctText = 0; //Number of correct answers
    BitmapFont correctBitmap; //Bitmap of ^
    private int streakText = 0; //Number of correct answers in a row
    BitmapFont streakBitmap; //Bitmap of ^
    private int badStreak = 0; //To summon Math Hero
    private String answerTopText = ""; //Answer in top section
    BitmapFont answerTopBitmap; //Bitmap of ^
    private String answerMidText = ""; //Answer in middle section
    BitmapFont answerMidBitmap; //Bitmap of ^
    private String answerBotText = ""; //Answer in bottom section
    BitmapFont answerBotBitmap; //Bitmap of ^
    private String questionText = ""; //Question
    BitmapFont questionBitmap; //Bitmap of ^
    private String tutorialText = "";
    BitmapFont tutorialBitmap;
    private int touchX = 8000; //These need to be initialized high so that we know that the button isnt being pressed
    private int touchY = 8000;
    private java.util.Random generator; //R.N.Gesus
    private int firstNumber; //Numbers used in RNG
    private int secondNumber; //^
    private int answer; //The actual answer
    private int fakeAnswerOne; //The two fake answers
    private int fakeAnswerTwo; //^
    private int topNumber; //The number in the top slot
    private int midNumber; //The number in the middle slot
    private int botNumber; //The number in the bottom slot
    private double speedMultiplier = 1.0; //Multiplies the speed as the game progresses
    
    @Override
    public void create() {
    	Gdx.input.setInputProcessor(this); //InputProcessor
    	
        batch = new SpriteBatch();
        //Math Man Setup
        textureAtlas = new TextureAtlas(Gdx.files.internal("data/spritesheets.atlas")); //Create the TextureAtlas for Math Man
        AtlasRegion region = textureAtlas.findRegion("0001"); //Find the region 0001 (the first frame)
        mathMan = new Sprite(region); //Create Math Man starting at frame 0001
        mathMan.setPosition(120, (Gdx.graphics.getHeight()/2)); //Start Math Man as X=120 and half the height of the screen
        mathMan.scale(3f); //Scale him up
        //Math Hero Setup
        textureMathHero = new TextureAtlas(Gdx.files.internal("data/mathhero.atlas")); //Create the TextureAtlas for Math Hero
        AtlasRegion heroRegion = textureMathHero.findRegion("0001"); //Find the region 0001 (the first frame)
        mathHero = new Sprite(heroRegion); //Create Math Man starting at frame 0001
        mathHero.setPosition(2000, (Gdx.graphics.getHeight()/2)); //Start Math Hero at X=2000 (to keep him out of the frame) and half the height of the screen
        mathHero.scale(3f); //Scale him up
        //
        background = new Texture(Gdx.files.internal("data/background.png")); //Set the background to background.png
        
        Timer.schedule(new Task(){
                @Override
                public void run() {
                    currentFrame++;
                    if(currentFrame > 4)
                        currentFrame = 1;
                    
                    currentAtlasKey = String.format("%04d", currentFrame);
                    mathMan.setRegion(textureAtlas.findRegion(currentAtlasKey));
                    mathHero.setRegion(textureMathHero.findRegion(currentAtlasKey));
                } //End run()
            }
            ,0,1/30.0f);

        correctBitmap = new BitmapFont(); //Create the Bitmaps for the fonts
        streakBitmap = new BitmapFont();
        answerTopBitmap = new BitmapFont();
        answerMidBitmap = new BitmapFont();
        answerBotBitmap = new BitmapFont();
        questionBitmap = new BitmapFont();
        tutorialBitmap = new BitmapFont();
        
        answerTopBitmap.getData().setScale(2, 2); //Scale up the text to a better size
		answerMidBitmap.getData().setScale(2, 2);
		answerBotBitmap.getData().setScale(2, 2);
		questionBitmap.getData().setScale(2, 2);
		correctBitmap.getData().setScale(2, 2);
		streakBitmap.getData().setScale(2, 2);
		tutorialBitmap.getData().setScale(2, 2);
        
        generator = new java.util.Random(); //Create the generator
        
        makeQuestion(); //Make a question (starts the loop)
    } //End create()

    @Override
    public void dispose() {
        batch.dispose();
        textureAtlas.dispose();
    }

    @Override
    public void render() {     
    		Gdx.gl.glClearColor(1, 1, 1, 1);
    		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
    		loopDraw(); //Sets Positions for render
    		
    		batch.begin();
    		if (correctText == 0) tutorialText = "Press the button to move up, if you reach the top of the screen you'll go back to the bottom. \nTry to fly into the correct answer.";
    		else if (correctText == 1) tutorialText = "As you get answers correct, the game will go faster. \nYou can see how many answers you have correct by looking\n at the counter on the top of the screen";
    		else if (correctText == 2) tutorialText = "If you get three wrong in a row, you'll get some help finding out the answer";
    		else if (correctText == 3) tutorialText = "The numbers are all randomly generated, so the game only ends when you've had enough";
    		else if (correctText == 4) tutorialText = "Remember, it isn't about how well you're doing, or how well you did in the past.\n It's all about the math!";
    		else if (streakText == 10) tutorialText = "Nice Job!";
    		else if (streakText == 15) tutorialText = "On a roll!";
    		else if (streakText == 20) tutorialText = "WOW!";
    		else tutorialText = "";
    		
    		batch.draw(background, 0, 0); //Background Draw
    		mathMan.draw(batch); //Math Man Draw
    		mathHero.draw(batch); //Math Hero Draw 
    		answerTopBitmap.draw(batch, answerTopText+"", 1600, 900); //Top answer
    		answerMidBitmap.draw(batch, answerMidText+"", 1600, 540); //Mid answer
    		answerBotBitmap.draw(batch, answerBotText+"", 1600, 180); //Bot answer
    		questionBitmap.draw(batch, questionText+"", 910, 540); //Question
    		correctBitmap.draw(batch, "Correct: "  + correctText, 910, 1060); //Top counter
    		streakBitmap.draw(batch, "Streak: " + streakText, 910, 1030); //Bot counter
    		tutorialBitmap.draw(batch, tutorialText+"", 200, 400); //Tutorial text
    		batch.end();
    } //End render
    
    private void loopDraw() {
    	Gdx.input.setInputProcessor(this); //InputProcessor
    	if (System.currentTimeMillis() - deltaCheck > .016666667) { //This is so that it displays at 60fps
    		if (mathMan.getY() > Gdx.graphics.getHeight() || mathMan.getY() <= 0) { //If it is past either side of the Y axis
	    		playerY = mathMan.getOriginY(); //Change player Y to equal half of the height
	    		mathMan.setY(playerY); //Move the player here 
	    	}
	    	if ((touchY > 1007) && (touchX < 73)) playerY += 6; //If the button is pressed
	    	if (playerX >= Gdx.graphics.getWidth()) { //If it is past the end of the x-axis
	    		checkAnswer(playerY); //Check answer
	    		mathMan.setX(mathMan.getOriginX()); //Set its X to the starting point
	    		mathMan.setY(mathMan.getOriginY()); //And its Y to the starting point
	    		playerX = mathMan.getOriginX();  //Set player X to its new cordinates
	    		playerY = mathMan.getOriginY(); //Set player Y to its new cordinates
	    	}
	    	else { //If it is not past the x-axis
	    		playerX += (3 * speedMultiplier); //Add three (times the multiplier) to the player's position
	    		mathMan.setX(playerX); //Move the player to that position
	    		mathMan.setY(playerY);
	    	}
    		if (mathHeroX < 2000) { //If mathHeroX is less than 2000 (when the streak is more than 3 it is set to 0, this keeps him from extending to far)
    			mathHeroX += ( 5 * speedMultiplier); //Move him very fast
    			mathHero.setX(mathHeroX); 
    		}
    		else mathHero.setX(2000);
    	} //End Time check
    		deltaCheck = System.currentTimeMillis(); //Set the deltaCheck variable to be the last time this was run
    } //End loopDraw()
    
    private void makeQuestion() {
    	refreshGenerator(System.currentTimeMillis() * 69);
    	firstNumber = generator.nextInt(11); //numbers 0-10
    	
    	refreshGenerator(System.nanoTime() * 69); //
    	secondNumber = generator.nextInt(11); //numbers 0-10
    	
    	refreshGenerator(System.currentTimeMillis());
    	int setFunction = generator.nextInt(1);
    	
    	refreshGenerator(System.currentTimeMillis() * 893);
    	int fakeNumberOne = generator.nextInt(11); //numbers 0-10
    	
    	refreshGenerator(System.currentTimeMillis() * 137);
    	int fakeNumberTwo = generator.nextInt(11); //numbers 0-10
    	
    	if (firstNumber < secondNumber && setFunction == 0) { // first + second
    		questionText = firstNumber + "+" + secondNumber;
    		answer = firstNumber+secondNumber;
    		fakeAnswerOne = firstNumber + fakeNumberOne;
    		fakeAnswerTwo = fakeNumberTwo + firstNumber;orderAnswers();
    	} //End If
    	
    	else if (firstNumber < secondNumber && setFunction == 1) { //second - first
    		questionText = secondNumber + "-" + firstNumber;
    		answer = secondNumber-firstNumber;
    		fakeAnswerOne = (fakeNumberOne-fakeNumberTwo)*(fakeNumberOne-fakeNumberTwo);
    		fakeAnswerTwo = fakeNumberTwo + firstNumber;
    	} //End If
    	
    	else if (firstNumber > secondNumber && setFunction == 0) { // first + second
    		questionText = firstNumber + "+" + secondNumber;
    		answer = firstNumber+secondNumber;
    		fakeAnswerOne = fakeNumberTwo + secondNumber;
    		fakeAnswerTwo = fakeNumberOne + firstNumber;
    	} //End If
    	
    	else if (firstNumber > secondNumber && setFunction == 1) { // first - second
    		questionText = firstNumber + "-" + secondNumber;
    		answer = firstNumber-secondNumber;
    		fakeAnswerOne = fakeNumberOne + secondNumber;
    		fakeAnswerTwo = fakeNumberOne + fakeNumberTwo;
    	} //End If
    	
    	else if (firstNumber == secondNumber && setFunction == 0) { // first + second
    		questionText = firstNumber + "+" + secondNumber;
    		answer = firstNumber+secondNumber;
    		fakeAnswerOne = fakeNumberTwo + firstNumber;
    		fakeAnswerTwo = fakeNumberOne + secondNumber;
    	} //End If
    	
    	else if (firstNumber == secondNumber && setFunction == 1) { // first - second
    		questionText = firstNumber + "-" + secondNumber;
    		answer = firstNumber - secondNumber;
    		fakeAnswerOne = fakeNumberOne + fakeNumberTwo;
    		fakeAnswerTwo = secondNumber + fakeNumberOne;
    	} //End If
    	orderAnswers();
} //End makeQuestion
    
    private void refreshGenerator (long seed) {
    	generator.setSeed(seed);
    }
    
    private void orderAnswers() { //Assigns our values to the strings so that they can be printed on the screen
    	refreshGenerator(System.currentTimeMillis() * 70);
    	int order = generator.nextInt(3); //0-2
    	
    	if (order == 0) {
			answerTopText = answer + ""; //These + ""s convert our ints to Strings
			topNumber = answer;
			answerMidText = fakeAnswerOne + "";
			midNumber = fakeAnswerOne;
			answerBotText = fakeAnswerTwo + "";
			botNumber = fakeAnswerTwo;
		}
		else if (order == 1) {
			answerTopText = fakeAnswerTwo + ""; //Top
			topNumber = fakeAnswerTwo;
			answerMidText = answer + ""; //Middle
			midNumber = answer;
			answerBotText = fakeAnswerOne + ""; //Bottom
			botNumber = fakeAnswerOne;
		}
		else {
			answerTopText = fakeAnswerOne + ""; //Top
			topNumber = fakeAnswerOne;
			answerMidText = fakeAnswerTwo + ""; //Middle
			midNumber = fakeAnswerTwo;
			answerBotText = answer + ""; //Bottom
			botNumber = answer;
		}
    } //End orderAnswers
    
    private void checkAnswer(double playerYposition) {
    	if (playerYposition > 812 && playerYposition < 995 && topNumber == answer) handleAnswer(true); //Top
    	else if (playerYposition > 450 && playerYposition < 630 && midNumber == answer) handleAnswer(true); //Middle
    	else if (playerYposition > 90 && playerYposition < 270 && botNumber == answer)  handleAnswer(true); //Bottom
    	else handleAnswer(false); //None
    } //End checkAnswer
    
    private void handleAnswer(boolean correct) {
    	if (correct) { //If they were correct
    		++correctText;
    		++streakText;
    		badStreak = 0;
    		mathHero.setX(2000);
    		
    		if (correctText < 12) speedMultiplier = speedMultiplier + .2;
    		makeQuestion();
    	}
    	else if (badStreak < 3) { //If they were wrong and the badstreak is less than three
    		streakText = 0;
    		++badStreak;
    		mathHero.setX(2000);
    	}
    	else { //If they were wrong and the badstreak is greater than 3
    		if (topNumber == answer) mathHero.setY(903); //Top
			else if (midNumber == answer) mathHero.setY(540); //Middle
			else if (botNumber == answer) mathHero.setY(175); //Bottom
    		//badStreakCheck = true;
			mathHeroX = 0;
    	}
    } //End handleAnswer()
    
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) { //Used to see if the player is pressing the button
		touchX = Gdx.input.getX();
		touchY = Gdx.input.getY();
		return true;
	}
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) { //Used to reset the touch so that the button is not constantly held
		touchX = 8000;
		touchY = 8000;
		return false;
	}
/////////////////////////Not used/////////////////////////
	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}
} //End class mathMain