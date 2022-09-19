/*Code created for Comp Sci 316 class, Fall of 2020
* Created by Group 6: 
* 	Zachary Mineau, Krissy Olson, Chelsea Salzsieder, and Josh Wilger
* 
* References:
* Almas Baimagambetov (almaslvl@gmail.com) https://www.youtube.com/watch?v=FVo1fm52hz0
* 	-Used for creating the basic structure of the program
* Jaryt Bustard https://www.youtube.com/watch?v=I1qTZaUcFX0&feature=emb_logo
* 	-Used for referencing similar game mechanics
*/

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.*;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class FlyingTankSimulator extends Application {
	
// GLOBAL VARIABLES
	
	// Creates javafx entities
	private BorderPane root = new BorderPane();
	private Pane gamePane = new Pane();
	private Label scoreLabel = new Label("Score: 0");
	
	// Create AudioClips for music and sound effects
	Media music = new Media(this.getClass().getResource("/audio/Country_Harmonics_Extended.mp3").toString());
	MediaPlayer playMusic = new MediaPlayer(music);
	AudioClip shoot = new AudioClip(this.getClass().getResource("/audio/shoot.mp3").toString());
	AudioClip hit = new AudioClip(this.getClass().getResource("/audio/hit.mp3").toString());
	AudioClip explode = new AudioClip(this.getClass().getResource("/audio/explode.mp3").toString());

	
	private int score = 0; // Used for score-keeping
	private int enemySpeed = 4; // Default speed of enemies
	private int previousTick; // Used for delay
	private boolean isPaused = false; // Used for pausing the game
	
	// Used for player movement and key-press management
	private boolean _w_key_down = false;
	private boolean _s_key_down = false;
	private boolean _space_key_down = false;
	private boolean _escape_key_down = false;
	
	// Game time counter declaration (used for the timing and delay of many objects)
	private int t = 0;
	
	// Creates a bunch of different sprites using the Sprite class
	private Sprite player = new Sprite(150, 300, 45, "player", "/images/Tank1.png");
	private Sprite background = new Sprite(0, 0, 800, "background", "/images/Background1.png");
	private Sprite pause = new Sprite(272, 272, 272, "pause", "/images/Pause2.png");
	private Sprite gameover = new Sprite(272, 240, 272, "gameover", "/images/GameOver.png");
	private Sprite spaceToContinue = new Sprite(180, 500, 64, "spaceToContinue", "/images/Continue.png");
	private Sprite explosion = new Sprite(64, 64, 64, "explosion", "/images/Explosion.png");
		
	// Creates an animation loop that updates the various mechanics of the game
	AnimationTimer timer = new AnimationTimer() {
		@Override
		public void handle(long now) {
			update(); // Runs every 16 ms
		}
	};
	
// METHODS
	
	// Used to create and update the objects on the javafx Pane
	private Parent createContent() {
		scoreLabel.setFont(Font.font("Verdana",30));
		root.setTop(scoreLabel);
		BorderPane.setAlignment(scoreLabel, Pos.CENTER);
		root.setBackground(new Background(new BackgroundFill(Color.CORNFLOWERBLUE, null, null)));
		
		// Sets the size of the application window
		gamePane.setPrefSize(800, 800);	
		
		// Used many times throughout code to add sprites visually
		gamePane.getChildren().add(background);
		gamePane.getChildren().add(player);
		
		// Starts the AnimationTimer and adds the Pane to the BorderPane
		timer.start();
		root.setCenter(gamePane);		
		return root;
	}
	
	// Creates enemies and puts them on the screen
	private void addEnemies() {
		// Duck enemy:
		if (t % 20 == 0) {
            Sprite s = new Sprite(
            	(int)gamePane.getWidth() + 100, 
            	ThreadLocalRandom.current().nextInt(0, ((int)gamePane.getHeight() - 30)), 
            	30, 
            	"enemy", 
            	"/images/Duck1.png"
            );
            // Puts the sprite on the screen
            gamePane.getChildren().add(s);
		}
		
		// Skyscraper enemy:
		if (t % 200 == 0) {
            Sprite s = new Sprite(
            	(int)gamePane.getWidth() + 100, 
            	ThreadLocalRandom.current().nextInt(0, ((int)gamePane.getHeight() - 150)), 
            	150, 
            	"obstacle", 
            	"/images/Building1.png"
            );
            // Puts the sprite on the screen
            gamePane.getChildren().add(s);
		}
	}
	
	// Switches the duck enemy images occasionally
	private void enemyAnimation(Sprite s) {
		Image Duck1 = new Image("/images/Duck1.png");
		Image Duck2 = new Image("/images/Duck2.png");
		
		if (t % 120 <= 25 && s.getImage() != Duck2) {
			s.setImage(Duck2);
		}
		if (t % 120 >= 60 && s.getImage() != Duck1) {
			s.setImage(Duck1);
		}
	}

	// Used to give Sprite(s) the ability to shoot
	private void shoot(Sprite who) {
		Sprite s = new Sprite(
			(int)who.getTranslateX() + (int)who.getBoundsInParent().getWidth(), 
			(int)who.getTranslateY(), 
			15, 
			who.type + "bullet", 
			"/images/Projectile.png"
		);
		gamePane.getChildren().add(s);
		
		// Play shooting sound
		shoot.play();
	}

	// When keys are down, the player moves, the tank shoots, and resets things after the player dies and presses space
	private void movePlayer() {
		if (_w_key_down && player.getTranslateY() > 0 && !isPaused) {
			player.moveUp();
		}
		if (_s_key_down && player.getTranslateY() + player.getFitHeight() < gamePane.getHeight() && !isPaused) {
			player.moveDown();
		}
		if (_space_key_down && !player.dead && !isPaused && t >= previousTick + 10) {
			shoot(player);
			previousTick = t;
		} 
		if (_space_key_down && player.dead && t >= previousTick + 80) {
			reset();
		}
	}
	
	// Resets values and clears the screen of objects
	private void reset() {
		score = 0;
		t = 0;
		enemySpeed = 4;
		previousTick = 0;
		player.setTranslateY(300);
		scoreLabel.setText("Score: " + score);
		gamePane.getChildren().clear();
		gamePane.getChildren().add(background);
		player.dead = false;
		gamePane.getChildren().add(player);
		playMusic.stop();
		playMusic.setRate(1);
		playMusic.play();
	}
	
	// Helper function for update() and gameOver() that is used to parse through all the sprites
	private List<Sprite> sprites() {
		return gamePane.getChildren().stream().map(n -> (Sprite)n).collect(Collectors.toList());
	}
	
	// Does stuff when player dies
	private void gameOver() {
 		// Game Over Screen/Press Space to Continue and explosion mechanic
		if (player.dead) {
			if (t >= previousTick + 40 && !sprites().contains(gameover)) {
				gamePane.getChildren().add(gameover);
			}
			if (t >= previousTick + 150 && !sprites().contains(spaceToContinue)) {
				gamePane.getChildren().add(spaceToContinue);
			}
			if (!sprites().contains(explosion)) {
				explosion.setTranslateX(player.getTranslateX());
				explosion.setTranslateY(player.getTranslateY());
				gamePane.getChildren().add(explosion);
			}
			// Replaces player Sprite with Dead Explosion Sprite
			if (t >= previousTick + 60 && sprites().contains(explosion)) {
				gamePane.getChildren().remove(explosion);
			}
		}
		
		// Puts the Game Over and Press Space to Continue text in front of everything
		if (player.dead && sprites().contains(gameover) && sprites().contains(spaceToContinue)) {
			gamePane.getChildren().remove(gameover);
			gamePane.getChildren().remove(spaceToContinue);
			gamePane.getChildren().add(gameover);
			gamePane.getChildren().add(spaceToContinue);
		}
	}

	// Does many of the core game mechanics mostly through the interaction of sprites with other sprites and calling other methods
	private void update() {
		addEnemies(); // Puts enemies on screen
		movePlayer(); // Does things with the player when keys are pressed on the keyboard
		gameOver(); // Does stuff when player dies
		
		// Game time incrementor
		t += 1;
		
		// Increases enemy speed over time
		if (t % 1000 == 0 && !player.dead) {
			enemySpeed += 1;
			if (score > 100) {
				playMusic.setRate(playMusic.getRate() + 0.11);
			}
		}

		// Iterates through all sprites and does collision and mechanics for each type
		sprites().forEach(s -> {
			switch (s.type) {
			
				// Player bullet collision
				case "playerbullet":
					s.moveRight(8);
					// Player bullet to Duck collision
					sprites().stream().filter(e -> e.type.equals("enemy")).forEach(enemy -> {
						if (s.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
							enemy.dead = true;
							s.dead = true;
							
							// Increments score when a duck is hit
							score += 1;
							scoreLabel.setText("Score: " + score);
							
							// Plays hit sound
							hit.play(0.6);
						}
					});
					// Player bullet to Skyscraper collision
					sprites().stream().filter(e -> e.type.equals("obstacle")).forEach(obstacle -> {
						if (s.getBoundsInParent().intersects(obstacle.getBoundsInParent())) {
							s.dead = true;	
						}
					});
					break;
					
				// Enemy collision and shooting mechanic
				case "enemy":

					s.moveLeft(enemySpeed);
					if (s.getBoundsInParent().intersects(player.getBoundsInParent()) && !player.dead) {
						player.dead = true;
						previousTick = t;
					}
					if (Math.random() < 0.1) {
						enemyAnimation(s);
					}
					break;
				
				// Obstacle (skyscraper) collision
				case "obstacle":
					s.moveLeft(enemySpeed - 1);
					if (s.getBoundsInParent().intersects(player.getBoundsInParent()) && !player.dead) {
						player.dead = true;
						previousTick = t;
					}
					break;
			}
		});
		
		// Play explode sound when player dies
		if (player.dead && sprites().contains(player)) {
			explode.play(0.6);
		}
		
		
		// Removes sprites when dead
        gamePane.getChildren().removeIf(n -> {
            Sprite s = (Sprite) n;
            return s.dead;
        });
        
        // Removes sprites when off-screen
        gamePane.getChildren().removeIf(n -> {
            Sprite s = (Sprite) n;
            return (s.getTranslateX() < -100);
        });
        gamePane.getChildren().removeIf(n -> {
            Sprite s = (Sprite) n;
            return (s.getTranslateX() > 900);
        });   
	}
	
	// Sets up the javafx stage
	@Override
	public void start(Stage stage) throws Exception {
		Scene scene = new Scene(createContent());
		Image Tank1 = new Image("/images/Tank1.png");
		Image Tank2 = new Image("/images/Tank2.png");
				
		// Loops and plays music
		playMusic.setStopTime(Duration.seconds(122.777));
		playMusic.setCycleCount(1000);
		playMusic.setVolume(0.5);
		playMusic.play();
		
		// Detects when a key is pressed down (or held) on the keyboard
		scene.setOnKeyPressed(p -> {
			switch (p.getCode()) {
				case W:
					_w_key_down = true;
					break;
				case S:
					_s_key_down = true;			
					if (player.getImage() != Tank2) {
						player.setImage(Tank2);
					}	
					break;
				case SPACE:
					_space_key_down = true;
					break;
				case ESCAPE:
					// Pauses/stops the AnimationTimer when ESCAPE is pressed
					if (!isPaused && !player.dead) {
						timer.stop();
						isPaused = true;
						gamePane.getChildren().add(pause);
						playMusic.pause();
					}
					break;
				default:
					break;
			}
		});
		
		// Detects when a key is released on the keyboard
		scene.setOnKeyReleased(r -> {
			switch (r.getCode()) {
				case W:
					_w_key_down = false;
					break;
				case S:
					_s_key_down = false;			
					if (player.getImage() != Tank1) {
						player.setImage(Tank1);
					}
					break;
				case SPACE:
					_space_key_down = false;
					break;
				case ESCAPE:
					// Resumes/starts the AnimationTimer when ESCAPE is released a second time
					if (isPaused && _escape_key_down) {
						timer.start();
						isPaused = false;
						gamePane.getChildren().remove(pause);
						_escape_key_down = false;
						playMusic.play();
					}
					if (isPaused) {
						_escape_key_down = true;
					}
					break;
				default:
					break;
			}
		});
		
		// Stage properties
		stage.setScene(scene);
		stage.setTitle("Flying Tank Simulator 1.10"); // Change number for each update
		stage.setResizable(false);
		stage.show();
	}

	
// MAIN METHOD
	
	public static void main(String[] args) {
		launch(args);
	}
}
