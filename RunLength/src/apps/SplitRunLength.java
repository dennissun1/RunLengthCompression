package apps;

import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SplitRunLength {
	
	//read/write diff from vanilla version-  in SplitRunLength, runlength comes first, then value(s)

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
		
		//read/write diff from vanilla version-  in SplitRunLength, runlength comes first, then value(s)
		
		ArrayList<int[][]> framesList = new ArrayList<>(numFrames);
		for(int i=0; i<numFrames; i++) {//populate framesList with empty frames
			framesList.add(new int[width][height]);
		}
		
		InputStream compressedFile = new FileInputStream(new File("/Users/Dennis Sun/Desktop/Classes/590/SRL_compressed.dat"));
		OutputStream decompressedFile = new FileOutputStream("/Users/Dennis Sun/Desktop/Classes/590/SRL_decompressed.dat");
		int z, runLength, value;
		
		for (int y=0; y<height; y++) {//decompressing
			for (int x=0; x<width; x++) {
				z=0;
				while(z<numFrames) {
					runLength=compressedFile.read();
					if(runLength>127) {//runlengths 128->255 are matching runs, 128 being length 2
						runLength-=126;
						value=compressedFile.read();
						for(int i=0; i<runLength; i++) {
							framesList.get(z)[x][y]=value;
							z++;
						}
					}else {//runlengths 0->127 are nonmatching runs, 0 being length 1
						runLength+=1;
						for(int i=0; i<runLength; i++) {
							framesList.get(z)[x][y]=compressedFile.read();
							z++;
						}
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
		
		//read/write diff from vanilla version-  in SplitRunLength, runlength comes first, then value(s)
		
		OutputStream compressedFile = new FileOutputStream(new File("/Users/Dennis Sun/Desktop/Classes/590/SRL_compressed.dat"));
		
		int numFrames = frames.size();
		int width = frames.get(0).length;
		int height = frames.get(0)[0].length;
		int runLength;//0-127 means nonmatching run, 128-255 means matching run
		
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				runLength=1;
				for(int z=0; z<numFrames-1; z++) {
					//this takes care of nonMatching runs
					
					if(frames.get(z)[x][y] != frames.get(z+1)[x][y]) {
						//for loop finds out how long runlength will be
						
						for(int offset=0; offset<numFrames-z-1; offset++) {//-1 because z goes 0-> 149, numFrames goes to 1->150
							if(frames.get(z+offset)[x][y] != frames.get(z+offset+1)[x][y]) {
								if(offset == 127) {//this means there are 128 comparisons, aka runlength > 128
									runLength=offset+1;//+1 here because we do want the last pixel, since it is part of the nonmatching run
									break;
								}
								if(offset==numFrames-z-2) {//next iteration of for loop breaks out; this is pixel at z=149

									runLength=offset+2;//+1 here because we do want the last pixel, since it is part of the nonmatching run
								} 
							}else {
								runLength=offset;//not +1 because we don't want to include the pixel at z+offset
													//because pixel at z+offset is part of the upcoming matching run
								break;
							}
						}
						
						compressedFile.write(runLength-1);//-1 because reading in 0 is decoded as 1 in decompression
						for(int i=0; i<runLength; i++) {
							compressedFile.write(frames.get(z+i)[x][y]);
						}
						z=z+runLength-1;//-1 because the for loop will incrememt z to the correct position
					}
					
					//this takes care of Matching runs
					
					else if(frames.get(z)[x][y] == frames.get(z+1)[x][y]) {//this takes care of nonMatching runs
						//for loop finds out how long runlength will be
						
						for(int offset=0; offset<numFrames-z-1; offset++) {//-1 because z goes 0-> 149, numFrames goes to 1->150
							if(frames.get(z+offset)[x][y] == frames.get(z+offset+1)[x][y]) {
								if(offset == 128) {//this means there are 129 comparisons, aka runlength > 129
									runLength=offset+1;//+1 here because we do want the last pixel, since it is part of the matching run
									break;
								}
								if(offset==numFrames-z-2) {//next iteration of for loop breaks out; this is pixel at z=149
									runLength=offset+2;//+2 here because we do want the last pixel, PLUS we are off by one since we stopped early (end of zstack)
								}
							}else {
								runLength=offset+1;//+1 here because we do want the last pixel, since it is part of the matching run
								break;
							}
						}
						
						compressedFile.write(runLength+126);
						compressedFile.write(frames.get(z)[x][y]);
						
						z=z+runLength-1;//-1 because outer for loop will increment z again to the correct value
						
						if(z==numFrames-2) {//edge case where z=148 ends a matching run, and we still have z=149 to put in
							compressedFile.write(0);
							compressedFile.write(frames.get(z+1)[x][y]);
						}
					}
					
					
				}
			}
		}

		compressedFile.close();
	}
}
