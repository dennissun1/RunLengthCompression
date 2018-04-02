package apps;

import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RunLength {

	public static void main(String[] args) throws IOException, FileNotFoundException {
		String base = "bunny";
		String filename="/Users/Dennis Sun/Desktop/Classes/590/" + base + ".450p.yuv";
		File file = new File(filename);
		int numFrames = 150;
		int width = 800;
		int height = 450;

		InputStream src = new FileInputStream(file);
		ArrayList<int[][]> framesList = new ArrayList<>(numFrames);
		

		System.out.println("Reading frames... ");
		for(int z=0; z<numFrames; z++) {
			int[][] currentFrame = new int[width][height];
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					currentFrame[x][y] = src.read();
					
				}
			}
			framesList.add(currentFrame);
		}
		src.close();
		System.out.println("Done reading frames! ");
		
		
		System.out.println("compressing...");
		compress(framesList);
		System.out.println("compressing done!");
		
		System.out.println("decompressing...");
		decompress(numFrames, width, height);
		System.out.println("all done!");
		
	}
	private static void decompress( int numFrames, int width, int height) throws IOException {
		
		ArrayList<int[][]> framesList = new ArrayList<>(numFrames);
		for(int i=0; i<numFrames; i++) {//populate framesList with empty frames
			framesList.add(new int[width][height]);
		}
		
		InputStream compressedFile = new FileInputStream(new File("/Users/Dennis Sun/Desktop/Classes/590/RL_compressed.dat"));
		OutputStream decompressedFile = new FileOutputStream("/Users/Dennis Sun/Desktop/Classes/590/RL_decompressed.dat");
		int z, runLength, value;
		
		for (int y=0; y<height; y++) {//decompressing
			for (int x=0; x<width; x++) {
				z=0;
				while(z<numFrames) {
					value=compressedFile.read();
					runLength=compressedFile.read();
					for(int i=0; i<runLength; i++) {
						framesList.get(z)[x][y]=value;
						z++;
					}
				}
			}
		}
		
		for(z=0; z<numFrames; z++) {//writing out
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					decompressedFile.write(framesList.get(z)[x][y]);
				}
			}
		}
		
		compressedFile.close();
		decompressedFile.close();
	}
	private static void compress(ArrayList<int[][]> frames) throws IOException {
		
		OutputStream compressedFile = new FileOutputStream(new File("/Users/Dennis Sun/Desktop/Classes/590/RL_compressed.dat"));
		
		int numFrames = frames.size();
		int width = frames.get(0).length;
		int height = frames.get(0)[0].length;
		int runLength = 1;
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				for(int z=0; z<numFrames; z++) {
					
					if(z==numFrames-1) {
						compressedFile.write(frames.get(z)[x][y]);
						compressedFile.write(runLength);
						runLength=1;
					}
					else if(frames.get(z)[x][y] == frames.get(z+1)[x][y] ) {//if true then we are still in a run
						runLength++;
					}
					else {//run has ended
						compressedFile.write(frames.get(z)[x][y]);
						compressedFile.write(runLength);
						runLength=1;
					}
					
				}
			}
		}

		compressedFile.close();
	}
}
