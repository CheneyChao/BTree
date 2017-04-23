package com.sample.tree;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sample.tree.BTree;
import com.sample.tree.BTree.DataNode;
import com.sample.tree.BTree.LeafNode;
import com.sample.tree.BTree.Node;
import com.sample.tree.BTree.TreeNode;

import junit.framework.TestCase;

public class BTreeTest extends TestCase {
	private SecureRandom random = new SecureRandom();

	public void testInsert() throws Exception {
		BTree bTree = new BTree(5);
		List<String> dataItems = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m");
		for(String item : dataItems){
			bTree.insert(new DataNode(item));
		}
		
		printTree(bTree);
		
		List<String> dataItems2 = Arrays.asList("9", "8", "7", "6", "5", "4", "3", "2", "1");
		for(String item : dataItems2){
			bTree.insert(new DataNode(item));
		}
		
		List<String> dataItems3 = Arrays.asList("e1", "e2", "e3", "e4", "e9", "e8", "e7", "e6", "e5");
		for(String item : dataItems3){
			bTree.insert(new DataNode(item));
		}
		
		printTree(bTree);
		
		List<DataNode> dataNodes = bTree.getAllData();
		assertEquals(dataItems.size() + dataItems2.size() + dataItems3.size(), dataNodes.size());
		
		for(String item : dataItems){
			DataNode searchedNode = bTree.search(item);
			assertEquals(item, searchedNode.getData());
		}
		
		for(String item : dataItems2){
			DataNode searchedNode = bTree.search(item);
			assertEquals(item, searchedNode.getData());
		}
		
		for(String item : dataItems3){
			DataNode searchedNode = bTree.search(item);
			assertEquals(item, searchedNode.getData());
		}
	}

	public void testRandomInsertCheck() throws Exception{
		int count = 0;
		BTree bTree = new BTree(5);
		while(count < 100000){
			String insertItem = getRandomString();
			DataNode dataNode = new DataNode(insertItem);
			DataNode insertedDataNode = bTree.insert(dataNode);
			if(insertedDataNode != dataNode){
				assertEquals(dataNode.getData(), insertedDataNode.getData());
			}else{
				if(dataNode != bTree.search(dataNode.getData())){
					System.out.println("Something is wrong.");
					insertedDataNode = bTree.insert(dataNode);
					DataNode restult = bTree.search(dataNode.getData());
				}
			}
			count ++;
		}
		printTree(bTree);
	}
	
	private String getRandomString(){
		return new BigInteger(60, random).toString(32);
	}
	
	private void printTree(BTree bTree){
		Node treeRoot = bTree.getTreeRoot();
		if(treeRoot == null)
			return;
		int depth = 0;
		List<Node> result = Arrays.asList(treeRoot);
		printTreeNodeWithDepth(depth, result);
		while(result.size() > 0 && !result.get(0).isLeafNode()){
			depth += 1;
			result = getSubNodes(result);
			printTreeNodeWithDepth(depth, result);
		}
		
		printDataNode(depth + 1, result);
	}
	
	private void printTreeNodeWithDepth(int depth, List<Node> nodes){
		System.out.println();
		System.out.print(depth + "    ");
		Node parent = null;
		System.out.print("[");
		for(Node node : nodes){
			if(parent != null && parent != node.getParent()){
				System.out.print("] ");
				System.out.print("[");
			}
			System.out.print(node.getData() + " ");
			parent = node.getParent();
		}
	}
	
	private void printDataNode(int depth, List<Node> nodes){
		System.out.println();
		System.out.print(depth + "    ");
		for(Node node : nodes){
			System.out.print("[");
			for(DataNode dataNode : ((LeafNode)node).getDataNodes()){
				System.out.print(dataNode.getData() + " ");
			}
			System.out.print("] ");
		}
	}
	
	private List<Node> getSubNodes(List<Node> nodes){
		List<Node> result = new ArrayList<Node>();
		for(Node node : nodes){
			result.addAll(((TreeNode)node).getSubNodes());
		}
		return result;
	}
	
	public void testInsertCheckParents() throws Exception {
		BTree bTree = new BTree(5);

		List<String> dataItems = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m");
		for(String item : dataItems){
			bTree.insert(new DataNode(item));
		}
		
		List<String> dataItems3 = Arrays.asList("e1", "e2", "e3" , "e4", "e9", "e8", "e7", "e6", "e5");
		for(String item : dataItems3){
			bTree.insert(new DataNode(item));
		}
		
		TreeNode treeRoot = (TreeNode)bTree.getTreeRoot();
		assertEquals(treeRoot.getParent(), null);
		
		for(Node node : treeRoot.getSubNodes()){
			assertEquals(treeRoot, node.getParent());
			if(!node.isLeafNode()){
				for(Node subNode: ((TreeNode)node).getSubNodes()){
					assertEquals(node, subNode.getParent());
				}
			}
		}
		
	}
}
