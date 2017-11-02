import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


	public class AppTest  extends TestCase {



	    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }


	public void testPrintHelloWorld() {


		System.out.println("AppTest Success");
		String s = "Hello World";
		assertEquals( s,"Hello World");
		System.out.println("AppTest Success");

	}

}
