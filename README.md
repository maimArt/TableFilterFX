# TableFilterFX
An extension for the JavaFX TableView that adds the possibility of applying a filter to the column headers

[![Travis Build Status](https://travis-ci.org/maimArt/TableFilterFX.svg?branch=master)](https://travis-ci.org/maimArt/TableFilterFX)

[![Maven Central](https://img.shields.io/maven-central/v/com.github.maimart/TableFilterFX.svg?maxAge=10000)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22TableFilterFX%22)

[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg?maxAge=2592000)]()

![screenshot1](https://cloud.githubusercontent.com/assets/20232625/16540990/0c2d9c2a-4078-11e6-87cc-98b96d7f017c.png)

## Maven dependency

```xml
<dependency>
        <groupId>com.github.maimart</groupId>
        <artifactId>TableFilterFX</artifactId>
        <version>1.0.0</version>
</dependency>
```

## Implementation

The implementation of the filter is quite easy. You wrap your TableView with the TableFilter
and add the columns that should be filterd by tableFilter.filterColumn(TableColumn column)

######1 Build your TableView like usual by code or fxml
```
TableView<Pojo> table = new TableView<>();
table.getItems().addAll(pojoList);
TableColumn<Pojo, String> columnA = new TableColumn<>("ColA");
TableColumn<Pojo, String> columnB = new TableColumn<>("ColB");
table.getColumns().add(columnA);
table.getColumns().add(columnB);	
..
```

######2 After that apply the filter

```
TableFilter<Pojo> tableFilter = new TableFilter<>(table);
tableFilter.filterColumn(columnA);
tableFilter.filterColumn(columnB);
```

**:grey_exclamation: You can initialize the table data by using TableView.getItems(), but you must not bind the TableView.itemsProperty().
If you want to bind the table data use the TableFilter.unfilteredItemsProperty() instead.**

## TODOs
This is a early version of the filter. If you find any bugs please feel free to create an issue. I´m glad to every reported bug.

- Deployment on center maven repository will come soon
- Improve performance on big lists (10k+)
- Intensive testing of the filter will happen in nearest future

