import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;

public class main_dataset_generation {

	
	static String dataSet_rep = "D:\\Users\\abdel\\CORPUS";
	static String[] years = {"2013", "2014", "2015" };
	
	public static String out_rep = "D:\\Users\\abdel\\CORPUS\\Subset";
	
	public static int number_files_by_year = 10000;
	
	//Given a directory, the function return all the files of the directory and subdirectory
	public static void listFiles(File dir, ArrayList<File> files)
	{
		File[] files_dir = dir.listFiles();
		
		if(files_dir == null)
			return;
		
		for (File file : files_dir)
		{		
			if (file.isDirectory())
				listFiles(file, files);
			else
				files.add(file);
		}
	}
	
	
	
	public static void generate_dataset()
	{
		
		for(String year : years)
		{
			ArrayList<File> files = new ArrayList<File>();
			
			listFiles(new File(dataSet_rep + "\\" + year), files);
			
			ArrayList<Integer> list_ids = new ArrayList<Integer>();
	        for (int i=0; i< files.size() ; i++) 
	        	list_ids.add(new Integer(i));
	        
	        Collections.shuffle(list_ids);
	        
	        
	        //on enregistre les number_files_by_yer documents dans out_rep
	        for(int i=0; i < number_files_by_year; i++)
	        {
	        	File file = files.get(list_ids.get(i));
	        	String name = file.getName();
	        	String mounth = name.substring(4,6);
	        	String day = name.substring(6,8);
	        	String new_path = out_rep + "/" + year + "/" + mounth + "/" + day +"/" + name;
	        	File new_file = new File(new_path);
	        	
	        	if (!new_file.exists())
					new File(new_file.getParentFile().getPath()).mkdirs();
	        	
	        	try {
					Files.copy(file.toPath(), new File(new_path).toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
		}
		
	}
	
	
	

	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		generate_dataset();
	}

}
