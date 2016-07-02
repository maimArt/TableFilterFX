package com.maimart.fx.tablefilter;

import java.util.ArrayList;
import java.util.List;

import com.maimart.fx.tablefilter.TableFilter;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class SampleFilteredTable extends Application {

	@Override
	public void start(Stage primaryStage) {
		TableView<Pojo> filteredTable = buildTable();
//		SimpleListProperty<Pojo> dummyList = new SimpleListProperty<>(FXCollections.observableArrayList());
//		filteredTable.itemsProperty().bind(dummyList);
		Scene scene = new Scene(filteredTable);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private TableView<Pojo> buildTable() {
		TableView<Pojo> table = new TableView<>();
		table.getItems().addAll(buildPojoList());
		TableColumn<Pojo, String> columnA = new TableColumn<>("ColA");
		TableColumn<Pojo, String> columnB = new TableColumn<>("ColB");
		columnA.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().a));
		columnB.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().b));
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
		for (int i = 0; i < 10; i++) {
			pojoList.add(new Pojo("A" + i, "B0"));
		}
		return pojoList;
	}

	private final static class Pojo {
		public String a;
		public String b;

		public Pojo(String a, String b) {
			this.a = a;
			this.b = b;
		}
	}

}
