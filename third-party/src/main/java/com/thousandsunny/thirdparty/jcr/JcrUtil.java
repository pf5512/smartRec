package com.thousandsunny.thirdparty.jcr;

import org.apache.jackrabbit.rmi.repository.URLRemoteRepository;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import sun.net.www.MimeTable;

import javax.jcr.*;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.util.Calendar;

import static java.util.Objects.isNull;
import static org.apache.jackrabbit.JcrConstants.*;
import static org.apache.jackrabbit.commons.JcrUtils.getBinaryProperty;
import static org.apache.jackrabbit.commons.JcrUtils.getStringProperty;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.FileCopyUtils.copy;
import static org.springframework.util.MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.util.ResourceUtils.CLASSPATH_URL_PREFIX;
import static org.springframework.util.ResourceUtils.getFile;
import static sun.net.www.MimeTable.getDefaultTable;

/**
 * Created by guitarist on 7/20/16.
 */
public class JcrUtil {
    private static Session session;

    @Test
    public void test() throws RepositoryException, MalformedURLException {
//        Repository repository = JcrUtils.getRepository();
        Repository repository = new URLRemoteRepository("http://localhost:8080/rmi");
        Session session = repository.login(new GuestCredentials());
        try {
            String user = session.getUserID();
            String name = repository.getDescriptor(Repository.REP_NAME_DESC);
            System.out.println("Logged in as " + user + " to a " + name + " repository.");
        } finally {
            session.logout();
        }
    }

    @Test
    public void test2() throws RepositoryException, MalformedURLException {
//        Repository repository = JcrUtils.getRepository();
        Repository repository = new URLRemoteRepository("http://localhost:8080/rmi");
        Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
        try {
            Node root = session.getRootNode();
            // Store content
            Node hello = root.addNode("hello");
            Node world = hello.addNode("world");
            world.setProperty("message", "Hello, World!");
            session.save();
            // Retrieve content
            Node node = root.getNode("hello/world");
            System.out.println("====================" + getStringProperty(session, "/hello/world/message", "default"));
            System.out.println("====================" + node.getPath());
            System.out.println("====================" + node.getProperty("message").getString());
            // Remove content
//            root.getNode("hello").remove();
            session.save();
        } finally {
            session.logout();
        }
    }

    private Logger logger = getLogger(getClass());

    @Test
    public void testIterator() throws MalformedURLException, RepositoryException {
        Node node = session().getRootNode();
        node.addNode("sites");
        node.addNode("channels");
        node.addNode("articles");
        NodeIterator nodeIterator = node.getNodes();
        while (nodeIterator.hasNext()) {
            Node n = nodeIterator.nextNode();
            logger.debug("Identify:{},Value:{}", n.getIdentifier(), n.getProperties());
        }
        session.save();
    }

    public static void saveFile(MultipartHttpServletRequest request) throws RepositoryException, FileNotFoundException {
//        request.getMultiFileMap().forEach((s, multipartFiles) -> multipartFiles.get(0).getInputStream());
        File file = getFile(CLASSPATH_URL_PREFIX + "1.jpg");
        MimeTable mt = getDefaultTable();
        String mimeType = mt.getContentTypeFor(file.getName());
        if (mimeType == null)
            mimeType = APPLICATION_OCTET_STREAM_VALUE;
        Node roseNode = session().getRootNode();
        Node fileNode = roseNode.addNode(file.getName(), NT_FILE);
        Node resNode = fileNode.addNode(JCR_CONTENT, NT_RESOURCE);
        resNode.setProperty(JCR_MIMETYPE, mimeType);
        resNode.setProperty(JCR_ENCODING, "");
        resNode.setProperty(JCR_DATA, new FileInputStream(file));
        Calendar lastModified = Calendar.getInstance();
        lastModified.setTimeInMillis(file.lastModified());
        resNode.setProperty(JCR_LASTMODIFIED, lastModified);
        session().save();
    }

    public static void readFile(HttpServletResponse response) {
        try {
            InputStream inputStream = getBinaryProperty(session().getRootNode(), "1.jpg/" + JCR_CONTENT + "/" + JCR_DATA, null).getStream();
            copy(inputStream, response.getOutputStream());
        } catch (IOException | RepositoryException e) {
            e.printStackTrace();
        }
    }

    private static Session session() {
        if (isNull(session)) {
            try {
                Repository repository = new URLRemoteRepository("http://localhost:8088/rmi");
                session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
            } catch (RepositoryException | MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return session;
    }
}
