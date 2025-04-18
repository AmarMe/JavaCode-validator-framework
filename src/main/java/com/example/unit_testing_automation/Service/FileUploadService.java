package com.example.unit_testing_automation.Service;

import com.example.unit_testing_automation.Model.TestFile;
import com.example.unit_testing_automation.Repository.FileUploadRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileUploadService {

    @Autowired
    private FileUploadRepository repository;

//    private static final String UPLOAD_DIR = "C:\\Users\\DELL\\Documents\\Practice\\BNY-Test-Automation-tool\\unit-testing-automation\\unit-testing-automation\\src\\main\\resources\\uploads\\";

    public ResponseEntity<String> uploadModule(MultipartFile file) throws IOException {
        List<String> methodsList = new ArrayList<>();

        String originalUploadedFileName = file.getOriginalFilename();
        Path tempDir = Files.createTempDirectory("compile_dir");
        System.out.println("Temp File Directory: "+tempDir);
        File tempJavaFile = new File(tempDir.toFile(), originalUploadedFileName);
        file.transferTo(tempJavaFile);
        tempJavaFile.deleteOnExit();

        String result = executeTests(tempJavaFile,originalUploadedFileName);

//        JavaParser parser = new JavaParser();
//        ParseResult<CompilationUnit> parseResult = parser.parse(tempJavaFile);
//        if(parseResult.isSuccessful() && parseResult.getResult().isPresent()){
//            CompilationUnit compilationUnit = parseResult.getResult().get();
//            List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
//
//            if(!classes.isEmpty()){
//                for(ClassOrInterfaceDeclaration className: classes){
//                    String clazzName = className.getNameAsString();
//                    System.out.println("Class file name: "+clazzName);
//
//                    List<MethodDeclaration> methods = className.getMethods();
//                    for(MethodDeclaration method: methods){
//                        String methodName = method.getNameAsString();
//                        methodsList.add(methodName);
//                        String testStatus = executeTests(tempJavaFile,originalUploadedFileName);
//                        TestFile testFile = new TestFile(clazzName,methodName,testStatus);
//                        repository.save(testFile);
//                    }
//                    System.out.println("Method names list: "+methodsList);
//                }
//            }
//        }
        return new ResponseEntity<>(result,HttpStatus.OK);
    }

    public String executeTests(File tempJavaFile, String fileName){
        try{
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            int compiledResult = compiler.run(null,null,null, tempJavaFile.getPath());
            File tempParentDir = tempJavaFile.getParentFile();
            if(compiledResult!=0)
                return "Error occurred while compiling java class file";

            String javaFileName = fileName.replace(".java","");
            String className = "com.example.unit_testing_automation.TestDataFile."+javaFileName;

            URLClassLoader classLoader= URLClassLoader.newInstance(new URL[]{tempParentDir.toURI().toURL()});
            Class<?> clazz = Class.forName(className,true,classLoader);
            Object instance = clazz.getDeclaredConstructor().newInstance();

            for(Method method: clazz.getDeclaredMethods()){
                if(method.getParameterCount()==0){
                    method.setAccessible(true);
                    Object methodResult =  method.invoke(instance);
                    String actualOutput = (methodResult!=null)? methodResult.toString() : "null";
                    String expectedOutput = getMethodExpectedOutput(method.getName());
                    String testStatus = (actualOutput.equals(expectedOutput))? "Passed" : "Failed";
                    String methodName = method.getName();
                    TestFile testFile = new TestFile(javaFileName,methodName,testStatus);
                    repository.save(testFile);
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred while running tests: "+e.getMessage());
        }
        return "File uploaded and tested successfully";
    }

    private String getMethodExpectedOutput(String methodName) {
        String expectedOutput = switch (methodName) {
            case "testingSimulation" -> "method testingSimulation tested successfully";
            case "addition" -> "Answer is: 20";
            case "multiplication" -> "Answer is: 110";
            default -> "";
        };
        return expectedOutput;
    }


    // Get All
    public List<TestFile> allTestReports() {
        List<TestFile> testFileList = repository.findAll();
        return testFileList;
    }

    //excel generator
    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<TestFile> testFile = repository.findAll();

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("UnitTesting");

        // Create Header
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Class_Name");
        headerRow.createCell(2).setCellValue("Method_Name");
        headerRow.createCell(3).setCellValue("Test_Status");


        // Add Data
        int rowNum = 1;
        for (TestFile tfs : testFile) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(tfs.getId());
            row.createCell(1).setCellValue(tfs.getClassName());
            row.createCell(2).setCellValue(tfs.getMethodName());
            row.createCell(3).setCellValue(tfs.getTestStatus());

        }
        // Auto-size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        // Set content type and headers
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Tests_report.xlsx");

        try {
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        workbook.close();
    }

}
