import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.junit.Test;

import java.util.Date;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.JsonUtil.prettyPrinter;
import static javax.annotation.processing.Completions.of;
import static org.apache.commons.lang3.RandomStringUtils.*;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.apache.jackrabbit.util.Text.md5;

/**
 * 如果这些代码有用，那它们是guitarist在13/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public class TestProc {

    @Test
    public void test() {
        Joiner joiner = Joiner.on(" and ");
        System.out.println(joiner.join(newArrayList("1", "2", "3", "344")));

        Splitter splitter = Splitter.on(" and ");
        System.out.println(splitter.split("1 and 2 and 3 and 344"));
    }

    @Test
    public void testRandom() {
        IntStream.range(1, 50).forEach(value -> {
            System.out.println(randomNumeric(10));
        });
        System.out.println("=========================================");
        System.out.println(randomAlphabetic(10));
        System.out.println(randomNumeric(10));
        System.out.println(randomAlphanumeric(10));
        System.out.println(randomAscii(10));
    }

    @Test
    public void testDate() {
        System.out.println(ISO_DATE_FORMAT.format(new Date()));
        System.out.println(ISO_DATETIME_FORMAT.format(new Date()));
        System.out.println(ISO_DATETIME_FORMAT.format(new Date()));
        System.out.println(md5(md5("0c93ba0b9ce5d173848970a5127703a1")));
    }

    @Test
    public void testMap() {
        prettyPrinter(of("1", "1235"));
    }

}
