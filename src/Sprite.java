import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Reusable Sprite class for adding visual objects to the game
	public class Sprite extends ImageView {
		boolean dead = false;
		final String type;
		private Image image;
		
		// Initializing the Sprite class
		Sprite(int x, int y, int size, String type, String location) {
			this.type = type;
			setTranslateX(x);
			setTranslateY(y);
			this.setFitHeight(size);
			this.setPreserveRatio(true);
			// Error catching for incorrect file location
			try {
				this.image = new Image(location);
				this.setImage(this.image);
			}
			catch (Exception e) {
				System.out.println("Error: Invalid sprite image location");
				e.printStackTrace();
			}
		}
		
		// Allows setImage method to accept Strings
		void setImage(String location) {
			this.setImage(location);
		}
		
		// Sprite movement
		void moveLeft(int amount) {
			setTranslateX(getTranslateX() - amount);
		}
		void moveRight(int amount) {
			setTranslateX(getTranslateX() + amount);
		}		
		void moveUp() {
			setTranslateY(getTranslateY() - 7);
		}
		void moveDown() {
			setTranslateY(getTranslateY() + 7);
		}
		
	}