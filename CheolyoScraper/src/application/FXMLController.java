package application;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import entry.UnicodeEntry;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import list.List;
import list.SinglyLinkedList;

public class FXMLController implements Initializable {
	// Fields of Sample.fxml
	@FXML
	private WebView webView;

	@FXML
	private TextField txtURL;
	@FXML
	private TextField txtYearsAmount;
	@FXML
	private TextField txtSeason;
	@FXML
	private TextField txtDirectory;
	@FXML
	private TextArea txaResult;

	@FXML
	private Button btnExecute;
	@FXML
	private Button btnMakeNewPage;
	@FXML
	private Button btnRefresh;
	@FXML
	private Button btnPlus;
	@FXML
	private Button btnMinus;

	@FXML
	private Label lblRecentDay;
	@FXML
	private Label lblTimeTaken;

	@FXML
	private CheckBox chkAddRef;
	@FXML
	private CheckBox chkDateless;
	@FXML
	private CheckBox chkWeirdSeasons;
	@FXML
	private CheckBox chkHeaderless;
	@FXML
	private CheckBox chkFileMode;

	private Random rand = new Random();
	private WebEngine engine;
	private double webZoom;
	private String strDate;
	private String strParent;
	private boolean isFirst = true;
	private boolean isEnd = false;
	private Instant start;
	private ChangeListener<Worker.State> listener;
	private Stack<Stack<String>> subpages = new Stack<Stack<String>>();
	private FileOutputStream fp;
	private static final int SECONDS_IN_MILISECONDS = 1000;
	private static final int RANGE_MILISECONDS_TO_WAIT = 7;
	private static final int BASE_SECONDS = 4;
	private static final int LOAD_ERROR_THRESHHOLD_MILISECONDS = 60 * SECONDS_IN_MILISECONDS;
	private static final String CHEOLYO_FILE = "C:\\Users\\he_xi\\eclipse-workspace\\CheolyoScraper\\files\\Tester.txt";
	private Timer loadErrorSolver = new Timer();
	private int intCounter = 0;
	private List<UnicodeEntry> ueHangeuls = new SinglyLinkedList<UnicodeEntry>();
	private final Runnable rabExit = () -> {
		engine.getLoadWorker().stateProperty().removeListener(listener);
		while (txaResult.getText().contains("\n\n\n")) {
			txaResult.setText(txaResult.getText().replace("\n\n\n", "\n\n"));
		}
		if (chkAddRef.isSelected() || isEnd) {
			txaResult.setText(txaResult.getText().trim() + "\n==註釋==\n{{reflist}}");
		} else {
			txaResult.setText(txaResult.getText().trim());
		}
		txaResult.setText(txaResult.getText().replace("}=", "}\n="));
		txaResult.setText(txaResult.getText().replace("}○", "}\n○"));
		txaResult.setText(txaResult.getText().replace("={", "=\n{"));
		Duration timeElapsed = Duration.between(start, Instant.now());
		lblTimeTaken.setText(
				"Time Taken: " + timeElapsed.toMinutes() + " minutes and " + timeElapsed.toMinutesPart() + " seconds.");
		strDate = null;
		isFirst = true;
		txtSeason.setText("");
		chkAddRef.setSelected(false);
		subpages = new Stack<Stack<String>>();

		InputStream input;
		int length;
		byte[] buffer = new byte[2048];
		try {
			String strOutputHangeul = "";
			for (UnicodeEntry ueElement : ueHangeuls) {
				strOutputHangeul += ueElement.getPrivateUnicodeKey() + ueElement.getHangeulValue() + "\n";
			}

			input = new ByteArrayInputStream(strOutputHangeul.getBytes("UTF-8"));
			length = 0;
			fp = new FileOutputStream(CHEOLYO_FILE);
			while ((length = input.read(buffer)) != -1) {
				fp.write(buffer, 0, length);
			}
			fp.close();
			input.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (chkFileMode.isSelected()) {
			try {
				input = new ByteArrayInputStream(txaResult.getText().getBytes("UTF-8"));
				length = 0;
				fp = new FileOutputStream(txtDirectory.getText() + "\\" + intCounter++ + ".txt");
				while ((length = input.read(buffer)) != -1) {
					fp.write(buffer, 0, length);
				}
				fp.close();
				input.close();

				loadErrorSolver.cancel();
				if (!isEnd) {
					execute();
				} else {
					Alert altFinish = new Alert(AlertType.CONFIRMATION);
					altFinish.setTitle("Execution Completed");
					altFinish.setHeaderText("Contents within TextArea are copied onto clipboard.");
					altFinish.showAndWait();
					Toolkit.getDefaultToolkit().getSystemClipboard()
							.setContents(new StringSelection(txaResult.getText()), null);
				}
				isEnd = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Alert altFinish = new Alert(AlertType.CONFIRMATION);
			altFinish.setTitle("Execution Completed");
			altFinish.setHeaderText("Contents within TextArea are copied onto clipboard.");
			altFinish.showAndWait();
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(txaResult.getText()),
					null);
		}
	};

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		if (webView != null) {
			engine = webView.getEngine();

			txtURL.setText("https://www.google.com");
			webZoom = 0.5;
			webView.setZoom(webZoom);

			browse(null);
//			try {
//				Scanner scnr = new Scanner(new FileInputStream(CHEOLYO_FILE), "UTF-8");
//				while (scnr.hasNext()) {
//					String strEntryLine = scnr.nextLine();
//					ueHangeuls.addFirst(new UnicodeEntry(strEntryLine.charAt(0), strEntryLine.substring(1)));
//				}
//				scnr.close();
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
		}
	}

	private void listenerSetter(Runnable rabCode) throws IOException, InterruptedException {
		listener = (obs, oldState, newState) -> {
			if (newState == State.SUCCEEDED) {
				// new page has loaded, process:
				rabCode.run();
			}
		};
	}

	public void execute() throws Exception {
		if (chkDateless.isSelected()) {
			listenerSetter(() -> nonDateRecursion());
			nonDateRecursion();
		} else {
			listenerSetter(() -> datedRecursion());
			datedRecursion();
		}
	}

	public void datedRecursion() {
		try {
			if (isFirst) {
				start = Instant.now();
			}
			loadErrorSolver.cancel();

			final WebEngine webengine = webView.getEngine();
			webengine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
				public void changed(ObservableValue ov, State oldState, State newState) {
					if (newState == Worker.State.SUCCEEDED) {
						Document doc = webengine.getDocument();
						try {
							Transformer transformer = TransformerFactory.newInstance().newTransformer();
							transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
							transformer.setOutputProperty(OutputKeys.METHOD, "xml");
							transformer.setOutputProperty(OutputKeys.INDENT, "yes");
							transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
							transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

							DOMResult domrResult = new DOMResult();
							transformer.transform(new DOMSource(doc), domrResult);
							txaResult.setText(paragraphRecursion(domrResult.getNode().getChildNodes()));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			});
			webengine.load(txtURL.getText());
		} catch (Exception e) {
			e.printStackTrace();
			loadErrorSolver = new Timer(false);
			loadErrorSolver.schedule(new TimerTask() {
				@Override
				public void run() {
					Platform.runLater(() -> {
						engine.load(txtURL.getText());
					});
				}
			}, LOAD_ERROR_THRESHHOLD_MILISECONDS, LOAD_ERROR_THRESHHOLD_MILISECONDS);
			txaResult.setText(txaResult.getText() + "Branch2");
			engine.load(txtURL.getText());
		}
	}

	public String paragraphRecursion(NodeList nl) {
		String strAdd = "";
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element elem = (Element) nl.item(i);
				if ("IMG".equals(elem.getNodeName())) {
					strAdd += "here";
				} else if ("SPAN".equals(elem.getNodeName())
						&& "idx_wrap idx_annotation03".equals(elem.getAttribute("class"))) {
					strAdd += "{{*|";
					strAdd += paragraphRecursion(elem.getChildNodes());
					strAdd += "}}";
					continue;
				}
			}
			if (nl.item(i).getChildNodes().getLength() == 0) {
				strAdd += nl.item(i).getTextContent();
			} else {
				strAdd += paragraphRecursion(nl.item(i).getChildNodes());
			}
		}
		return strAdd;
	}

	public void nonDateRecursion() {
		try {
			if (isFirst) {
				start = Instant.now();
				txaResult.setText("");
			}
			loadErrorSolver.cancel();

			engine.getLoadWorker().stateProperty().addListener(listener);
			// Obtains Website Info
			// Wait about 7 seconds, then continue on with response getting.
			Thread.sleep((BASE_SECONDS + rand.nextInt(RANGE_MILISECONDS_TO_WAIT)) * SECONDS_IN_MILISECONDS);

			// Getting information on the next website.
			HttpResponse<String> response = HttpClient
					.newHttpClient().send(
							HttpRequest.newBuilder().GET().header("accept", "application/json")
									.uri(URI.create(engine.getLocation())).build(),
							HttpResponse.BodyHandlers.ofString());

			// Going to the next website.
			String strBody = response.body();
			if (strBody.contains("/newchar")) {
				int intI = strBody.indexOf("/newchar");
				if (strBody.indexOf("/newchar", strBody.indexOf('\n', intI)) != -1) {
					intI = strBody.indexOf('\n', strBody.indexOf('\n', intI));
				}
				int i = 0;
				while (strBody.indexOf("/newchar", intI + 1) != -1) {
					intI = strBody.indexOf("/newchar", intI + 1);
					i++;
				}
				txaResult.setText(txaResult.getText() + "newchar" + i);
			}
			Document dcPage = engine.getDocument();

			// Makes Header
			Element nlClass = dcPage.getElementById("cont_area");
			NodeList nlText = nlClass.getElementsByTagName("P");
			NodeList nlH3 = nlClass.getElementsByTagName("H3");
			String strTitle = nlH3.item(0).getTextContent();
			int intStart = strTitle.lastIndexOf("/");
			String strParent = "";
			if (intStart != -1) {
				strTitle = strTitle.substring(intStart + 1).trim();
				strParent = nlH3.item(0).getTextContent().substring(0, intStart);
			}
			if (!(strParent.equals(this.strParent) || isFirst)) {
				this.strParent = strParent;
			}
			isFirst = false;
			int intHeaderLevel = 2;
			intStart = 0;
			while (strParent.indexOf("/", intStart + 1) != -1) {
				intHeaderLevel++;
				intStart = strParent.indexOf("/", intStart + 1);
			}
			for (int i = 1; i <= intHeaderLevel; i++) {
				strTitle = "=" + strTitle + "=";
			}
			// Get sub pages (if any)
			intStart = strBody.length() - 1;
			Stack<String> subpage = new Stack<String>();
			int intEnd;
			while (strBody.lastIndexOf("<li><a href=\"/id/wga_", intStart - 1) != -1) {
				intStart = strBody.lastIndexOf("<li><a href=\"/id/wga_", intStart) + "<li><a href=\"".length();
				intEnd = strBody.indexOf(">", intStart) - 1;
				subpage.push("http://sillok.history.go.kr" + strBody.substring(intStart, intEnd));
				intStart -= "<li><a href=\"".length() + 1;
			}
			subpages.push(subpage);

			// Formats the String
			String strAdd = "";
			intStart = strBody.lastIndexOf("<p class='paragraph'>");
			intStart = strBody.lastIndexOf("\n", intStart);
			intStart = strBody.indexOf("<p class='paragraph'>", intStart) + "<p class='paragraph'>".length();
			intEnd = strBody.indexOf("<", intStart);
			String strHeader = strBody.substring(intStart, intEnd);

			int intStarters = 0;
			while (intStart != -1) {
				intStarters++;
				intStart = strBody.indexOf(strHeader, intStart + 1);
			}
			// Paragraph getter
			for (int i = nlText.getLength() - 1; i >= 0; i--) {
				String strElement = nlText.item(i).getTextContent();
				if (strElement.contains(strHeader)) {
					intStart = strElement.indexOf(strHeader);
					while (intStart != -1) {
						intStarters--;
						intStart = strElement.indexOf(strHeader, intStart + 1);
					}
				}
				strAdd = "\n\n" + strElement.trim() + strAdd;
				if (intStarters == 0) {
					break;
				}
			}
			strAdd = strAdd.trim();
			if (!chkHeaderless.isSelected()) {
				strAdd = strTitle + "\n" + strAdd;
			}

			strAdd = strFormater(strAdd);
			// End of String formation

			// String of interest is added to TextArea.
			txaResult.setText(txaResult.getText() + strAdd + "\n\n");

			// Downloads images
			intStart = strBody.lastIndexOf("<p class='paragraph'>");
			intStart = strBody.lastIndexOf("\n", intStart);
			intStart = strBody.indexOf("/images/slkimg/", intStart);
			int intImages = 1;
			while (intStart != -1 && intStart < strBody.indexOf("\n", intStart)) {
				// Wait about 7 seconds, then download.
				Thread.sleep((BASE_SECONDS + rand.nextInt(RANGE_MILISECONDS_TO_WAIT)) * SECONDS_IN_MILISECONDS);
				String strImageWeb = strBody.substring(intStart, strBody.indexOf("\"", intStart));
				URL urlImage = new URL("http://sillok.history.go.kr" + strImageWeb);
				InputStream input = urlImage.openStream();
				OutputStream output = new FileOutputStream(
						txtDirectory.getText() + "\\" + strTitle + "_" + intImages++ + ".jpg");
				byte[] buffer = new byte[2048];
				int length = 0;
				while ((length = input.read(buffer)) != -1) {
					output.write(buffer, 0, length);
				}
				input.close();
				output.close();
				intStart = strBody.indexOf("/images/slkimg/", intStart + 1);
			}

			while (!subpages.isEmpty() && subpages.lastElement().isEmpty()) {
				subpages.pop();
			}
			String strNextLink = "http://sillok.history.go.kr/id/"
					+ strBody.substring(strBody.indexOf("goNext('") + "goNext('".length(),
							strBody.indexOf("'", strBody.indexOf("goNext('") + "goNext('".length()));
			;
			if (!subpages.isEmpty()) {
				subpage = subpages.pop();
				strNextLink = subpage.pop();
				subpages.push(subpage);
			}
			if (!strBody.contains("goNext('")) {
				isEnd = true;
				rabExit.run();
				return;
			}

			// Wait about 7 seconds, then go to website.
			engine.getLoadWorker().stateProperty().addListener(listener);
			Thread.sleep((BASE_SECONDS + rand.nextInt(RANGE_MILISECONDS_TO_WAIT)) * SECONDS_IN_MILISECONDS);
			engine.load(strNextLink);
			loadErrorSolver = new Timer(false);
			loadErrorSolver.schedule(new TimerTask() {
				@Override
				public void run() {
					Platform.runLater(() -> {
						txaResult.setText(txaResult.getText() + "LoadError");
						engine.reload();
					});
				}
			}, LOAD_ERROR_THRESHHOLD_MILISECONDS, LOAD_ERROR_THRESHHOLD_MILISECONDS);
		} catch (Exception e) {
			loadErrorSolver = new Timer(false);
			loadErrorSolver.schedule(new TimerTask() {
				@Override
				public void run() {
					Platform.runLater(() -> {
						txaResult.setText(txaResult.getText() + " LoadError");
						engine.reload();
					});
				}
			}, LOAD_ERROR_THRESHHOLD_MILISECONDS, LOAD_ERROR_THRESHHOLD_MILISECONDS);
			txaResult.setText(txaResult.getText() + " LoadError");
			engine.reload();
		}
	}

	@FXML
	void browse(KeyEvent event) {
		if (event == null || event.getCode().equals(KeyCode.ENTER)) {
			engine.load(txtURL.getText());
		}
	}

	public void refresh() {
		engine.reload();
	}

	public void zoomIn() {

		webZoom += 0.1;
		webView.setZoom(webZoom);
	}

	public void zoomOut() {
		webZoom -= 0.1;
		webView.setZoom(webZoom);
	}

	public void datelessClicked() {
		if (chkDateless.isSelected()) {
			chkHeaderless.setDisable(false);
		} else {
			chkHeaderless.setDisable(true);
		}
	}

	public void newMainPage() {
		int intMax = Integer.parseInt(txtYearsAmount.getText());
		String strAdd = "*[[/總序|總序]]\n*[[/卽位年|卽位年]]\n";
		for (int i = 1; i <= intMax; i++) {
			if (i != 1) {
				strAdd += "*[[/" + strIntToChinese("" + i) + "年|" + strIntToChinese("" + i) + "年]]\n";
			} else {
				strAdd += "*[[/元年|元年]]\n";
			}
		}
		strAdd += "*[[/附錄|附錄]]\n";
		txaResult.setText(strAdd);
	}

	public void chkFileModeClicked() {
		intCounter = 0;
	}

	private String strIntToChinese(String strString) {
		String strIntToReplace = "";
		int intIntTemp;
		int intIntToReplace;

		for (Character chrElement : strString.toCharArray()) {
			if (Character.isDigit(chrElement)) {
				strIntToReplace += chrElement;
			}
		}

		intIntToReplace = Integer.parseInt(strIntToReplace);
		strIntToReplace = "";
		intIntTemp = intIntToReplace;
		switch (intIntTemp / 10) {
		case 2:
			strIntToReplace += '二';
			break;
		case 3:
			strIntToReplace += '三';
			break;
		case 4:
			strIntToReplace += '四';
			break;
		case 5:
			strIntToReplace += '五';
			break;
		case 6:
			strIntToReplace += '六';
			break;
		case 7:
			strIntToReplace += '七';
			break;
		case 8:
			strIntToReplace += '八';
			break;
		case 9:
			strIntToReplace += '九';
			break;
		}
		if (intIntTemp / 10 != 0) {
			intIntTemp %= 10;
			strIntToReplace += '十';
		}
		switch (intIntTemp) {
		case 1:
			strIntToReplace += '一';
			break;
		case 2:
			strIntToReplace += '二';
			break;
		case 3:
			strIntToReplace += '三';
			break;
		case 4:
			strIntToReplace += '四';
			break;
		case 5:
			strIntToReplace += '五';
			break;
		case 6:
			strIntToReplace += '六';
			break;
		case 7:
			strIntToReplace += '七';
			break;
		case 8:
			strIntToReplace += '八';
			break;
		case 9:
			strIntToReplace += '九';
			break;
		}
		if (intIntToReplace == 1) {
			strIntToReplace = "正";
		}
		return strString.replace("" + intIntToReplace, strIntToReplace);
	}

	private String strFormater(String strAdd) {
		engine.getLoadWorker().stateProperty().removeListener(listener);
		int intColonFront = strAdd.indexOf(":\n\n");
		while (intColonFront != -1) {
			strAdd = strAdd.replace(strAdd.substring(0, intColonFront + 3),
					strAdd.substring(0, intColonFront) + "：{{quote|");
			intColonFront = strAdd.indexOf("\n", intColonFront);
			if (intColonFront == -1) {
				strAdd += "}}";
				break;
			} else {
				strAdd = strAdd.replace(strAdd.substring(0, intColonFront), strAdd.substring(0, intColonFront) + "}}");
			}

			intColonFront = strAdd.indexOf(":\n\n");
		}

		strAdd = strAdd.replaceAll(" ", "").replaceAll(",", "，").replaceAll("【", "").replaceAll("】", "")
				.replaceAll(":", "：").replaceAll("‘", "『").replaceAll("’", "』").replaceAll("ㆍ", "、")
				.replaceAll("!", "！").replaceAll("\\?", "？").replaceAll(";", "；").replaceAll("/", "，")
				.replaceAll("〔", "gg").replaceAll("〕", "g").replaceAll("\\)", "\\)\\)").replace('·', '、')
				.replaceAll("\\)\\)g", "\\)g").replaceAll("〉", "b").replace("〈", "bb");
		if (!strAdd.contains("○")) {
			if (strAdd.lastIndexOf("=\n") == -1) {
				strAdd = "○" + strAdd;
			} else {
				int intOInsertionPoint = strAdd.lastIndexOf("=\n") + 2;
				strAdd = strAdd.replace(strAdd.substring(intOInsertionPoint),
						"○" + strAdd.substring(intOInsertionPoint));
			}
		}
		int intStringIterator = strAdd.indexOf('"');
		boolean isFirstDouble = true;
		for (UnicodeEntry e : ueHangeuls) {
			strAdd = strAdd.replaceAll("" + e.getPrivateUnicodeKey(), e.getHangeulValue());
		}
		while (intStringIterator != -1) {
			if (isFirstDouble) {
				strAdd = strAdd.replace(strAdd.substring(0, intStringIterator + 1),
						strAdd.substring(0, intStringIterator) + '「');
			} else {
				strAdd = strAdd.replace(strAdd.substring(0, intStringIterator + 1),
						strAdd.substring(0, intStringIterator) + '」');
			}
			intStringIterator = strAdd.indexOf('"');
			isFirstDouble = !isFirstDouble;
		}
		intStringIterator = 0;
		while (intStringIterator < strAdd.length()) {
			if (Character.UnicodeBlock
					.of(strAdd.charAt(intStringIterator)) == Character.UnicodeBlock.PRIVATE_USE_AREA) {
				try {
					FXMLLoader loader = new FXMLLoader(getClass().getResource("HangeulMaker.fxml"));
					Parent ptHangeul = loader.load();
					HangeulController hcInstance = loader.getController();
					hcInstance.setChar(strAdd.charAt(intStringIterator));
					hcInstance.setChar(strAdd.charAt(intStringIterator));
					hcInstance.setPageName(engine.getLocation());

					Stage stHangeul = new Stage();
					stHangeul.setScene(new Scene(ptHangeul));
					stHangeul.initModality(Modality.APPLICATION_MODAL);
					stHangeul.initOwner(btnExecute.getScene().getWindow());
					stHangeul.showAndWait();
					ueHangeuls.addLast(new UnicodeEntry(hcInstance.getChar(), hcInstance.getHangeul()));
					strAdd = strAdd.replaceAll("" + hcInstance.getChar(), hcInstance.getHangeul());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			intStringIterator++;
		}
		strAdd = strAdd.replaceAll("，『", "：『").replaceAll("，「", "：「");

		return strAdd;
	}
}
