package com.example.task2;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class HelloApplication extends Application {
    Random random = new Random();
    Scanner sc = new Scanner(System.in);
    private Connection connection = null;
    private Statement statement = null ;
    Boolean connected = true;
    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Music");

        Label username = new Label("Username: ");
        username.setMaxWidth(Double.MAX_VALUE);
        TextField userText = new TextField();
        userText.setMaxWidth(Double.MAX_VALUE);

        Label password = new Label("Password: ");
        password.setMaxWidth(Double.MAX_VALUE);
        TextField passwordText = new TextField();
        passwordText.setMaxWidth(Double.MAX_VALUE);

        Label confirm = new Label();

        Button connect = new Button("Connect");
        connect.setAlignment(Pos.CENTER_RIGHT);

        connect.setOnAction(event->{
            String userN = userText.getText();
            String passcode = passwordText.getText();
            connectToDB(userN,passcode,confirm);
            if(connected){
                ArrayList<String> listOfAll = new ArrayList<>();
                try {
                    searchAlbum(listOfAll);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                ObservableList<String> albums = FXCollections.observableArrayList();
                albums.add(0,"AID         TITLE         YEAR");
                for(int i=0;i<listOfAll.size();i++){
                    albums.add(i+1,listOfAll.get(i));
                }
                ListView<String> albumList = new ListView<>(albums);
                VBox homePage = new VBox();

                Scene root = new Scene(homePage);
                Label search = new Label("Search");
                TextField searchTitle = new TextField("Enter Album title");
                searchTitle.setOnAction(event2 -> {
                    ArrayList<String> albumByTitle = new ArrayList();
                    try {
                        searchAlbumByTitle(albumByTitle,searchTitle.getText());
                        ObservableList<String> newAlbums = FXCollections.observableArrayList();
                        newAlbums.add(0,"AID         TITLE         YEAR");
                        for(int i=0;i<albumByTitle.size();i++){
                            newAlbums.add(i+1,albumByTitle.get(i));
                        }
                        ListView<String> newAlbumList = new ListView<>(newAlbums);
                        createStage(newAlbumList,stage,root);
                        Button backBtn = new Button("<");
                        backBtn.setOnAction(event4 ->{
                            stage.setScene(root);
                            stage.show();
                        });
                        VBox albumPage = new VBox();
                        albumPage.getChildren().addAll(backBtn,newAlbumList);
                        stage.setScene(new Scene(albumPage));
                        stage.show();
                    } catch (SQLException e) {
                        System.out.println("Something went wrong, couldn't search by title"+e.getMessage());
                    }
                });
                createStage(albumList,stage,root);
                Label searchSong = new Label("Search Song: ");
                TextField searchSongTxt = new TextField("Enter word/phrase in the title of a song");
                searchSongTxt.setOnAction(event3 ->{

                    ArrayList<Song> songs = new ArrayList<>();
                    String phrase = searchSongTxt.getText();
                    TableView<Song> songsTable = new TableView<>();
                    TableColumn<Song,Integer> songId = new TableColumn<>("Song_ID");
                    TableColumn<Song,Integer> albumId = new TableColumn<>("Album_ID");
                    TableColumn<Song,String> songNo = new TableColumn<>("Song_No");
                    TableColumn<Song,String> songName = new TableColumn<>("Name");
                    TableColumn<Song,String> artist = new TableColumn<>("Artist");
                    TableColumn<Song,String> length = new TableColumn<>("Length");

                    songId.setCellValueFactory(new PropertyValueFactory<>("songId"));
                    albumId.setCellValueFactory(new PropertyValueFactory<>("albumId"));
                    songNo.setCellValueFactory(new PropertyValueFactory<>("songNo"));
                    songName.setCellValueFactory(new PropertyValueFactory<>("Name"));
                    artist.setCellValueFactory(new PropertyValueFactory<>("artist"));
                    length.setCellValueFactory(new PropertyValueFactory<>("length"));

                    try {
                        searchSongsWithTitle(songs,phrase);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    songsTable.getColumns().addAll(songId,albumId,songNo,songName,artist,length);
                    ObservableList<Song> list = FXCollections.observableArrayList(songs);

                    songsTable.setItems(list);
                    Button backBtn = new Button("<");
                    backBtn.setOnAction(event4 ->{
                        stage.setScene(root);
                        stage.show();
                    });
                    VBox songsPage = new VBox();
                    songsPage.getChildren().addAll(backBtn,songsTable);
                    stage.setScene(new Scene(songsPage));
                    stage.show();
                });
                HBox searchField = new HBox();
                searchField.getChildren().addAll(search,searchTitle);

                HBox searchSongField = new HBox();
                searchSongField.getChildren().addAll(searchSong,searchSongTxt);

                Button addAlbum = new Button("Add new Album");

                homePage.getChildren().addAll(searchField,searchSongField,addAlbum,albumList);
                stage.setScene(root);
                stage.show();
            }
        });

        GridPane grid = new GridPane();
        grid.add(username,0,0);
        grid.add(userText,1,0);
        grid.add(password,0,1);
        grid.add(passwordText,1,1);
        grid.add(connect,0,2,2,1);
        grid.add(confirm,0,3,2,1);

        stage.setScene(new Scene(grid));
        stage.show();
    }
    public void createStage(ListView<String> albumList, Stage stage, Scene previousScene){
        ArrayList<String> songs = new ArrayList<>();
        albumList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    try {
                        searchSongsOnAlbum(songs,newValue);
                        ObservableList<String> newSongs = FXCollections.observableArrayList();
                        newSongs.add(0,"SID      AID      SongNo      Name     Artist     Length");
                        for(int i=0;i<songs.size();i++){
                            newSongs.add(i+1,songs.get(i));
                        }
                        Button backBtn = new Button("<");
                        backBtn.setOnAction(event -> {
                            stage.setScene(previousScene);
                            stage.show();
                        });
                        ListView<String> songList = new ListView<>(newSongs);
                        VBox root = new VBox();
                        Button edit = new Button("Edit album");
                        edit.setOnAction(event-> {
                            Label label = new Label();
                            int albumId = Integer.parseInt(getIdOfAlbum(newValue));
                            TextField newTitle = new TextField("Enter new title");
                            newTitle.setOnAction(event10 ->{
                                String title = newTitle.getText();
                                try {
                                    editAlbum(albumId,title, label);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            Button newbackBtn = new Button("<");
                            newbackBtn.setOnAction(event12 -> {
                                stage.setScene(previousScene);
                                stage.show();
                            });
                            VBox editedInfo = new VBox();
                            editedInfo.getChildren().addAll(newbackBtn,newTitle,label);
                            stage.setScene(new Scene(editedInfo));
                            stage.show();
                        });
                        Button add = new Button("Add song");

                        Button delete = new Button("Delete album");

                        HBox manipulationBtns = new HBox();
                        manipulationBtns.getChildren().addAll(add,edit,delete);
                        root.getChildren().addAll(backBtn,manipulationBtns,songList);
                        stage.setScene(new Scene(root));
                        stage.show();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
    public void addAlbum(String albumTitle,int year,int numSongs){
        try {
            String Asql = "INSERT INTO Album VALUES('" +albumTitle+ "','"+year+"')";
            statement.execute(Asql,Statement.RETURN_GENERATED_KEYS);
            ResultSet set = statement.getGeneratedKeys();
            set.next();
            int albumId = set.getInt(1);
            System.out.println("Album created");
            for(int x=1;x<=numSongs;x++){
                int songNumber =x;
                sc = new Scanner(System.in);
                Label titleLabel =new Label("Enter song title:");
                TextField titleText = new TextField("Title");
                HBox titles = new HBox();
                titles.getChildren().addAll(titleLabel,titleText);
                Label artistLabel =new Label("Enter the name of the artist");
                TextField artistText = new TextField("artist");
                HBox artists = new HBox();
                artists.getChildren().addAll(artistLabel,artistText);
                Label lengthLabel = new Label("How long is the song (in minutes)");
                TextField songLength = new TextField("Length");
                HBox lengths = new HBox();
                lengths.getChildren().addAll(lengthLabel,songLength);
                String Ssql = "INSERT INTO Track VALUES('"+albumId+"','"+songNumber+"','"+titleText.getText()+"','"+artistText.getText()+"','"+Float.parseFloat(songLength.getText())+"')";
                statement.execute(Ssql);
                System.out.println("Song "+songNumber+" inserted.");
                VBox newScene = new VBox();
                newScene.getChildren().addAll(titles,artists,lengths);
                Scene root = new Scene(newScene);
                break;
            }
        } catch (SQLException e) {
            System.out.println("Could not insert new record... " + e.getMessage());
        }
        System.out.println();
    }
    public void editAlbum(int albumId, String newTitle, Label update) throws SQLException {
        String sql = "UPDATE ALBUM SET Title = '"+newTitle+ "' WHERE AID = '"+albumId+"'";
        statement.execute(sql);
        update.setText("Title edited");
    }
    public void searchSongsWithTitle(ArrayList<Song> songs,String phrase) throws SQLException {
        String sql = "SELECT * FROM Track WHERE Name LIKE '%"+phrase+"%'";
        ResultSet result = statement.executeQuery(sql);
        System.out.println("SID"+"\t  "+"AID"+"\t   "+"SongNo"+"\t   "+"Name"+"\t   "+"Artist"+"\t   "+"Length");
        while(result.next()){
            Song song = new Song(result.getInt("SID"),
                    result.getInt("AID"),
                    result.getInt("SongNo"),
                    result.getString("Name"),
                    result.getString("Artist"),
                    result.getFloat("Length"));
            songs.add(song);
        }
    }
    public String getIdOfAlbum(String albumInfo){
        String aid = "";
        for(int i=0;i<albumInfo.length();i++){
            if(albumInfo.charAt(i) == ' '){
                break;
            }
            else {
                aid = aid + albumInfo.charAt(i);
            }
        }
        return aid;
    }
    public void searchSongsOnAlbum(ArrayList<String> songs,String albumName) throws SQLException {
        String aid = "";
        for(int i=0;i<albumName.length();i++){
            if(albumName.charAt(i) == ' '){
                break;
            }
            else {
                aid = aid + albumName.charAt(i);
            }
        }
        String sql = "SELECT * FROM Track WHERE AID = '"+aid+"'";
        ResultSet result = statement.executeQuery(sql);
        //System.out.println("SID"+"\t  "+"AID"+"\t   "+"SongNo"+"\t   "+"Name"+"\t   "+"Artist"+"\t   "+"Length");
        while(result.next()){
            songs.add(result.getInt("SID")+"\t   "+
                    result.getInt("AID")+"\t   "+
                    result.getInt("SongNo")+"\t   "+
                    result.getString("Name")+"\t   "+
                    result.getString("Artist")+"\t   "+
                    result.getFloat("Length"));
        }
    }
    public void searchAlbum(ArrayList<String> listOfAlbums) throws SQLException {
        String sql = "SELECT * FROM Album";
        ResultSet result = statement.executeQuery(sql);
        //System.out.println("AID"+"\t   "+"Title"+"\t   "+"Year");
        while(result.next()){
            listOfAlbums.add((result.getInt("AID")+"   "+
                    result.getString("Title")+"   "+
                    result.getInt("Year")));
        }
    }
    public void searchAlbumByTitle(ArrayList<String> listOfAlbums, String title) throws SQLException {
        String sql = "SELECT * FROM Album WHERE Title LIKE '%"+title+"%';";
        ResultSet result = statement.executeQuery(sql);
        //System.out.println("AID"+"\t   "+"Title"+"\t   "+"Year");
        while(result.next()){
            listOfAlbums.add((result.getInt("AID")+"   "+
                    result.getString("Title")+"   "+
                    result.getInt("Year")));
        }
    }
    public void connectToDB(String username,String password, Label confirm){
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Connect to MS sql server");
        if(true){
            System.out.println("Locate database to open");
            String connectionString = "jdbc:sqlserver://postsql.mandela.ac.za\\WRR;database=WRAP301Music";
            try {
                connection = DriverManager.getConnection(connectionString,username,password);
                statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
            } catch (SQLException e) {
                confirm.setText("   Unable to connect to DB... '%s'\n");
                connected = false;
            }
        }
        else{

        }
    }
    public void disconnectDB() {
        System.out.println("Disconnecting from database...");

        try {
            //Important to close connection (same as with files)
            connection.close();
        } catch (Exception ex) {
            System.out.println("   Unable to disconnect from database");
        }
    }
    public static void main(String[] args) {
        launch();
    }
}