package application;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class HangeulController {
	private Character charPrivate;

	private String strPageName;

	private String strHangeul;

	@FXML
	private TextField txtHangeul;

	@FXML
	private Button btnHangeul;
	@FXML
	private Button btnCopyChar;

	@FXML
	private Button btnCopyWebpage;

	@FXML
	private Label lblCurrentPage;

	/**
	 * Sets the strPageName field.
	 * 
	 * @param strPageName the strPageName to set
	 */
	public void setPageName(String strPageName) {
		this.strPageName = strPageName;
	}

	/**
	 * Sets the charPrivate field.
	 * 
	 * @param charPrivate the charPrivate to set
	 */
	public void setChar(Character charPrivate) {
		this.charPrivate = charPrivate;
	}

	/**
	 * Sets the charPrivate field.
	 * 
	 * @param charPrivate the charPrivate to set
	 */
	public Character getChar() {
		return charPrivate;
	}

	public void copyChar() {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection("" + charPrivate), null);
	}

	public void copyPage() {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(strPageName), null);
	}

	public void setHangeul(ActionEvent e) {
		Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
		strHangeul = txtHangeul.getText();
		stage.close();
	}

	public String getHangeul() {
		return strHangeul;
	}
}
