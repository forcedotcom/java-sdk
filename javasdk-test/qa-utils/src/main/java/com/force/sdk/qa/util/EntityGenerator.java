/**
 * Copyright (c) 2011, salesforce.com, inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 * 
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 * 
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *   
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.force.sdk.qa.util;
 
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.util.MissingResourceException;

/**
 * 
 * This class generates some basic entities for subquery tests. It can be repurposed if it's useful
 * for other tests.
 *
 * @author Jill Wetzler
 */
public final class EntityGenerator {
    
    private static final String LICENSE_FILE_URL = "/license/header.txt";
    private static String path;
   
    private EntityGenerator() {  }
    
    /**
     * 
     * This test creates:
     * Entity0 - name and id fields, OneToMany collections for every subsequent entity
     * EntityN where N > 1 - name and id fields, lookup field to Entity0,
     *                       lookup field to Entity(N-1), and OneToMany collection for Entity(N+1)
     * All OneToMany's use the lazy fetch type with the exception of the very last entity created.
     * 
     * If you want to add more than 26 entities this file will need to be adjusted slightly,
     * as you cannot have more than 25 child relationships
     * 
     * @param args is a String array that should have the following elements:
     * args[0] - the path to the folder where the entities should be created
     * args[1] - the number of entities to create
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        path = args[0];
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        } else {
            File[] files = dir.listFiles();
            for (File f : files) {
                f.delete();
            }
        }
        
        URL licUrl = EntityGenerator.class.getResource(LICENSE_FILE_URL);
        if (licUrl == null) {
            throw new MissingResourceException("Could not find license file " + LICENSE_FILE_URL + " on classpath.",
                                                LICENSE_FILE_URL, LICENSE_FILE_URL);
        }
        
        int numEntitiesToCreate = Integer.valueOf(args[1]);
        for (int i = 0; i < numEntitiesToCreate; i++) {
            File f = new File(path + "/Entity" + i + ".java");

            f.createNewFile();
            FileWriter fw = new FileWriter(f);
            BufferedWriter out = new BufferedWriter(fw);
            try {
                writeLicense(out, licUrl);
                out.write("/** GENERATED CLASS **/\n\n");
                out.write("package com.force.sdk.jpa.entities.generated;\n");
                out.write("import javax.persistence.*;\n");
                out.write("import java.util.*;\n\n");
                out.write("/**\n *\n * GENERATED ENTITY, do not edit or check in.\n *\n * @author Jill Wetzler\n */\n");
                out.write("@Entity\n");
                out.write("public class Entity" + i + " {\n");
                out.write("    @Id\n");
                out.write("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
                out.write("    public String id;\n");
                out.write("    public String getId() { return id; }\n");
                out.write("    public void setId(String id) { this.id = id; }\n\n");
                
                out.write("    public String name;\n");
                out.write("    public String getName() { return name; }\n");
                out.write("    public void setName(String name) { this.name = name; }\n\n");
                
                //Entity0 should have collections for all child entities
                if (i == 0) {
                    for (int j = 1; j < numEntitiesToCreate; j++) {
                        String type = String.format("Entity%s", j);
                        String field = String.format("entity%ss", j);
                        out.write("    @OneToMany(mappedBy = \"entity0\", fetch = FetchType.LAZY)\n");
                        out.write(String.format("    private Collection<%s> %s;\n", type, field));
                        out.write(String.format("    public Collection<%s> get%ss() { return %s; }\n\n", type, type, field));
                    }
                } else {
                    //create a lookup to Entity0 and the previous entity
                    if (i != 1) {
                        out.write("    @Column(name = \"entity0\")\n");
                        out.write("    @ManyToOne\n");
                        out.write("    public Entity0 entity0;\n");
                        out.write("    public Entity0 getEntity0() { return entity0; }\n");
                        out.write("    public void setEntity0(Entity0 entity0) { this.entity0 = entity0; }\n\n");
                    }
                    
                    String type = String.format("Entity%s", i - 1);
                    String field = String.format("entity%s", i - 1);
                    out.write(String.format("    @Column(name = \"%s\")\n", field));
                    out.write("    @ManyToOne\n");
                    out.write(String.format("    public %s %s;\n", type, field));
                    out.write(String.format("    public %s get%s() { return %s; }\n", type, type, field));
                    out.write(String.format("    public void set%s(%s %s) { this.%s = %s; }\n\n",
                                                    type, type, field, field, field));
                }
                
                //create list of future entities
                if (i != 0 && i != numEntitiesToCreate - 1) {
                    String mappedBy = String.format("entity%s", i);
                    String type = String.format("Entity%s", i + 1);
                    String field = String.format("entity%ss", i + 1);
                    String fetchType = numEntitiesToCreate - i < 4 ? "EAGER" : "LAZY"; //just make the last two OneToMany eager
                    out.write(String.format("    @OneToMany(mappedBy = \"%s\", fetch = FetchType.%s)\n", mappedBy, fetchType));
                    out.write(String.format("    private Collection<%s> %s;\n", type, field));
                    out.write(String.format("    public Collection<%s> get%ss() { return %s; }", type, type, field));
                }
                
                out.write("\n}\n");
                f.setWritable(false);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                out.close();
                fw.close();
            }
        }
    }
    
    /**
     * Helper to prepend the license header to generated entity files.
     * @param writer
     * @throws IOException 
     */
    private static void writeLicense(Writer writer, URL license) throws IOException {
        InputStream licIs = license.openStream();
        DataInputStream dis = new DataInputStream(licIs);
        BufferedReader blicReader = new BufferedReader(new InputStreamReader(dis));
        try {
            String line;
            writer.write("/**\n");
            while ((line = blicReader.readLine()) != null) {
                writer.write(" * " + line + "\n");
            }
            writer.write(" */\n\n");
        } finally {
            blicReader.close();
        }
    }
}
