module CheolyoScraper {
	requires javafx.controls;
	requires javafx.fxml;
	requires java.datatransfer;
	requires java.net.http;
	requires java.xml;
	requires javafx.web;
	requires java.desktop;
	requires javafx.base;
	requires javafx.graphics;
	
	opens application to javafx.graphics, javafx.fxml;
}
