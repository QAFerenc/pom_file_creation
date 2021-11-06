package mainPackage;

/* This little program shows a way of storing the DOM locators of Web Elements in the Page Object Model (POM) files.

   Usually people creating Automated Selenium tests makes the following steps manually :

        1. Moving mouse cursor over the Web Element, whose locator will be stored in the POM file
        2. Press Mouse Right Button
        3. Select Inspect from the list (usually tha last choice) : the Code Inspector opens
          (In the Code Inspector the relevant DOM section will be highlighted)
        4. Press Mouse Right button on the highlighted text, a list will come up with the possible locators
        5. Select one of the locators.
        6. Copy the selected locator with Ctrl-V to the POM (Java or other) file
        7. Make the POM file to be a valid Java file (add the necessary imports, square brackets, etc.)


   With the following code user can copy the locator and create the POM file with only 2 steps :

        1. (as above)
        2. Mouse Left Button click
 */


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class mainTest {


    public  static ChromeDriver driver;

    //Variables result and lastResult are for storing the clipboard content of the Code Inspector
    static String result = "";

    static String lastResult = "";

    static BufferedWriter myWriter = null;

    static Boolean sameContent = false;

    static Vector<String> vector = new Vector<String>();

    @BeforeClass
    public static void beforeClass() {

        System.setProperty("webdriver.chrome.driver","C:\\Users\\hp\\chromedriver2\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10000, TimeUnit.MILLISECONDS);

    }


    public static void  getClipboardContents() {

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        Transferable contents = clipboard.getContents(null);
        boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);

        if (hasTransferableText)
        {
            try {
                result = (String)contents.getTransferData(DataFlavor.stringFlavor);

                // Copy the locator only when the highlighted text in Code Inspector has changed.
                if(!result.equals(lastResult)) {
                    lastResult = result;

                    int startId=result.indexOf("id=");
                    if(startId >= 0) {
                        String id_substr = result.substring(startId + 4, result.length());
                        id_substr = id_substr.substring(0, id_substr.indexOf("\""));
                        System.out.println("//*[@id=\"" + id_substr + "\"]" + "\n");

                        int startName = result.indexOf("name=");
                        String name_substr = result.substring(startName + 6, result.length());
                        name_substr = name_substr.substring(0, name_substr.indexOf("\""));
                        System.out.println("//*[@name=\"" + name_substr + "\"]" + "\n");

                        vector.add(name_substr);

                        try {
                            myWriter.write("@FindBy(\"//*[@id=\"" + id_substr + "\"]" + "\"" + ")" + "\n");
                            myWriter.write("private WebElement " + name_substr + ";"+"\n"+"\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            catch (UnsupportedFlavorException | IOException ex){
                System.out.println(ex);
                ex.printStackTrace();
            }
        }
    }


    @Test
    public  void openMyBlog() throws AWTException, InterruptedException, IOException {

        Robot robot = new Robot();
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\hp\\chromedriver\\chromedriver.exe");

        String text = new String();

        driver.get("https://www.facebook.com/");

        // Wait for some seconds till the actual programm starts

        Thread.sleep(5000);

        // Delete the existing POM file

        File file = new File("pom_file.java");
        file.delete();


        // As the POM file is a Java file, create the necessary imports for the file.
        // The structure of the file is a valid Java file

        myWriter = new BufferedWriter(new FileWriter("pom_file.java", true));


        myWriter.write("package testProject.pageObjects;\n" +
                "\n" +
                "import org.openqa.selenium.support.FindBy;\n" +
                "import org.openqa.selenium.WebDriver;\n" +
                "import org.openqa.selenium.support.ui.WebDriverWait;\n" +
                "import javax.xml.ws.wsaddressing.W3CEndpointReference;\n" +
                "\n" +
                "public class TestPage  {\n" +
                "\n" );

//        myWriter.close();

        Point p = MouseInfo.getPointerInfo().getLocation();


        // The programm ends, when the user moves the mouse cursor to the top left part of the screen

        while( !((p.x==0) && (p.y==0)))
        {

            // With the following Robot keypress, the Code Inspector opens

            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_C);

            Thread.sleep(1500);

            robot.keyRelease(KeyEvent.VK_CONTROL);
            robot.keyRelease(KeyEvent.VK_C);
            robot.keyRelease(KeyEvent.VK_SHIFT);

  //          myWriter = new BufferedWriter(new FileWriter("pom_file.java", true));

            // Copy the DOM part of the actual element from the Code Inspector

            Thread.sleep(1500);

            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_C);

            Thread.sleep(500);

            robot.keyRelease(KeyEvent.VK_CONTROL);
            robot.keyRelease(KeyEvent.VK_C);

            myWriter.flush();

            Thread.sleep(500);


            // Check the clipboard content only when the mouse cursor is not on the top left position (this would mean quit)

            if(!((p.x==0) && (p.y==0)))
                getClipboardContents();

           // myWriter.close();

            // Get the mouse position again for the check of the quit condition at while

            p = MouseInfo.getPointerInfo().getLocation();


        }



    }

    @AfterClass
    public static void afterClass() throws IOException {

        // Finish the POM file : create the getters for the stored locators and close the file with character }"

        //myWriter = new BufferedWriter(new FileWriter("pom_file.java", true));

        myWriter.write("\n\n\n");
        myWriter.write("//getters "+"\n\n");

        for (String x : vector) {
            myWriter.write("public void get_"+x+"() ");
            myWriter.write("{"+"\n");
            myWriter.write("    return "+x+";"+"\n");
            myWriter.write("}");
            myWriter.write("\n\n");
        }


        myWriter.write("}");


        myWriter.close();

        // quit from the browser driver

        driver.quit();
    }
}