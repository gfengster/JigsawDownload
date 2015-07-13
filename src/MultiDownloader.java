import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.abc.download.DownloadManager;
import com.abc.download.Status;
import static java.lang.System.out;

public class MultiDownloader {

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        
       // String address = "http://repo.flexiant.com/images/public/iso/ipxe.iso";
        String address = "http://boot.ipxe.org/ipxe.iso";
        String dest = "/tmp/i.iso";
        String real = "/home/gf/ipxe.iso";
        
        DownloadManager mgr = DownloadManager.getManager(dest, address, 20);
        
        mgr.download();
        
        while (mgr.getStatus() != Status.FINISHED.name()) {
            out.println(mgr.getTotalSize() + " " + mgr.getDownloadedSize());
            
            Thread.sleep(10);
        }
        
        final String md5MultiDownloader = getMD5Sum(dest);
        out.println("Download with MultiDownloader: " + md5MultiDownloader);
        
        final String md5Chrome = getMD5Sum(real);
        out.println("Download with Chrome: " + md5Chrome);
        
        out.println(md5MultiDownloader.equalsIgnoreCase(md5Chrome)? "Match" : "Not Match");
        
        out.println(("Elapsed time: " + (System.currentTimeMillis() - start)));
        
    }

	private static String getMD5Sum(String filename) throws NoSuchAlgorithmException, IOException {
		InputStream is = Files.newInputStream(Paths.get(filename));
		
		final MessageDigest md = MessageDigest.getInstance("MD5");
		
		DigestInputStream dis = new DigestInputStream(is, md);
		
		int len = 0;
		while((len = dis.available()) > 0) {
			dis.read(new byte[len]);
		}
		
		final byte[] array = md.digest();
		
		return new BigInteger(1, array).toString(16);
	}
}
