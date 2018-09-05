package me.lucas.kits.orm.redis.jedis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import me.lucas.kits.commons.utils.Charsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * Created by zhangxin on 2018/9/4-下午9:03.
 *
 * @author zhangxin
 * @version 1.0
 */
public class LuaTest {
    private String script;
    private Jedis jedis;

    @Before
    public void prepareScript() throws URISyntaxException {
        //        File file = new File(this.getClass().getClassLoader().getResource("CompareAndCache.lua").toURI());
        //        Long fileLength = file.length();
        //        byte[] filecontect = new byte[fileLength.intValue()];
        //        try (FileInputStream fr = new FileInputStream(file)) {
        //            fr.read(filecontect);
        //        } catch (Exception e) {
        //            System.out.println(e.getMessage());
        //        }
        //        script = new String(filecontect, Charsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        try (InputStream is = AbstractRedisClient.class.getClassLoader().getResourceAsStream("CompareAndCache.lua");
                BufferedReader br = new BufferedReader(new InputStreamReader(is));) {
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
            script = sb.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        jedis = new Jedis("127.0.0.1", 6379);
    }

    @Test
    public void select() {
        Object eval = jedis.eval(script, 1, "KEY_18600000001", "VALUE1", "3600", "SELECT");
        System.out.println("select:" + eval);
    }

    @Test
    public void update() {
        Object eval = jedis
                .eval(script, 1, "KEY_18600000001", "VALUE1", "3600", "UPDATE", System.currentTimeMillis() + "",
                        "3600");
        System.out.println("update:" + eval);
    }

    @Test
    public void update2() {
        Object eval = jedis.eval(script, 1, "KEY_18600000001", "VALUE2", "3600", "UPDATE", "1536115992341", "3600");
        System.out.println("update2:" + eval);
    }

    @After
    public void after() {
        jedis.close();
    }
}
