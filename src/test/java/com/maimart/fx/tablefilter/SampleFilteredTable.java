package com.maimart.fx.tablefilter;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

public class SampleFilteredTable extends Application {

	@Override
	public void start(Stage primaryStage) {
		TableView<Pojo> filteredTable = buildTable();
		// SimpleListProperty<Pojo> dummyList = new
		// SimpleListProperty<>(FXCollections.observableArrayList());
		// filteredTable.itemsProperty().bind(dummyList);
		Scene scene = new Scene(filteredTable);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private TableView<Pojo> buildTable() {
		TableView<Pojo> table = new TableView<>();
		table.setEditable(true);
		table.getItems().addAll(buildPojoList());
		TableColumn<Pojo, String> columnA = new TableColumn<>("ColA");
		TableColumn<Pojo, String> columnB = new TableColumn<>("ColB");
		columnA.setCellValueFactory(new PropertyValueFactory<Pojo, String>("a"));
		columnB.setCellValueFactory(new PropertyValueFactory<Pojo, String>("b"));
		columnA.setCellFactory(TextFieldTableCell.forTableColumn());
		columnB.setCellFactory(TextFieldTableCell.forTableColumn());
		columnA.setOnEditCommit(event -> event.getRowValue().setA(event.getNewValue()));
		columnB.setOnEditCommit(event -> event.getRowValue().setB(event.getNewValue()));
		columnA.setSortable(true);
		columnB.setSortable(true);
		table.getColumns().add(columnA);
		table.getColumns().add(columnB);
		columnA.prefWidthProperty().bind(table.widthProperty().multiply(0.49));
		columnB.prefWidthProperty().bind(table.widthProperty().multiply(0.49));

		TableFilter<Pojo> tableFilter = new TableFilter<>(table);
		tableFilter.filterColumn(columnA);
		tableFilter.filterColumn(columnB);

		return table;
	}

	public static void main(String[] args) {
		launch(args);
	}

	private List<Pojo> buildPojoList() {
		List<Pojo> pojoList = new ArrayList<>();
		for (int j = 0; j < 1; j++) {
			for (int i = 0; i < 20; i++) {
				pojoList.add(new Pojo("A" + i, "B0"));
			}
		}

		return pojoList;
	}

	public final static class Pojo {
		private final StringProperty a = new SimpleStringProperty();
		private final StringProperty b = new SimpleStringProperty();

		public Pojo(String a, String b) {
			this.a.set(a);
			this.b.set(b);
		}

		public String getA() {
			return this.a.get();
		}

		public void setA(final String a) {
			this.a.set(a);
		}

		public String getB() {
			return this.b.get();
		}

		public void setB(final String b) {
			this.b.set(b);
		}

	}

}
