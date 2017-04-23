
package com.sample.tree;

import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class BTree {
	
	private int degree = 5;
	private LeafNode leafNodePointer;
	private Node treeRoot;
	
	public BTree(){
		this.degree = 5;
	}
	
	public BTree(int degree) throws Exception{
		if(degree < 5 || degree > 20){
			throw new Exception("Invalid degree, accepted range is from 5 to 20");
		}
		this.degree = degree;
	}
	
	public Node getTreeRoot(){
		return treeRoot;
	}
	
	public int getDepth(){
		int depth = 0;
		if(treeRoot == null)
			return depth;
		Node node = treeRoot;
		if(node.isLeafNode())
			return 1;
		depth += 1;
		while(!node.isLeafNode()){
			node = ((TreeNode)node).getSubNodes().get(0);
			depth += 1;
		}
		depth += 1;
		return depth;
	}
	
	public DataNode search(String data){
		if(treeRoot == null)
			return null;
		return treeRoot.search(data);
	}
	
	/*
	 * If the key exists, update it. Otherwise, insert it.
	 */
	public DataNode insert(DataNode dataNode) throws Exception{
		if(StringUtils.isEmpty(dataNode.data)){
			throw new Exception("The insert data cannot be empty");
		}
		
		if(treeRoot == null){
			treeRoot = leafNodePointer =  new LeafNode(this);
			leafNodePointer.insert(dataNode);
		}

		return treeRoot.insert(dataNode);
	}
	
	void setTreeRoot(Node newRootNode){
		Objects.requireNonNull(newRootNode);
		treeRoot = newRootNode;
	}
	
	public List<DataNode> getAllData(){
		List<DataNode> nodes = new ArrayList<DataNode>(degree);
		LeafNode node =  leafNodePointer;
		if(node == null)
			return nodes;
		nodes.addAll(node.getDataNodes());
		while((node = node.getNextNode()) != null){
			nodes.addAll(node.getDataNodes());
		}
		return nodes;
	}
	

	
	static abstract class Node{
		private TreeNode parent;
		
		private BTree bTree;
		
		public Node(BTree bTree){
			this.bTree = bTree;
		}

		protected void setParent(TreeNode parent) {
			this.parent = parent;
		}
		
		public TreeNode getParent() {
			return parent;
		}
		
		
		protected BTree getBTree() {
			return bTree;
		}
		
		public abstract String getData();
		public abstract DataNode search(String data);
		public abstract boolean isLeafNode();
		public abstract DataNode insert(DataNode dataNode);
		protected int getSplitIndex(){
			if(bTree.degree % 2 == 0){
				return bTree.degree/2;
			}else{
				return (bTree.degree + 1)/2;
			}
		}
		
		protected int getSplitedRightSideSize(){
			if(bTree.degree % 2 == 0){
				return bTree.degree/2 - 1;
			}else{
				return (bTree.degree - 1)/2;
			}
		}
		
		protected void propagate(Node newRightNode) {
			// ensure the node has data
			if(!this.isLeafNode() && this.getData() == null){
				TreeNode treeNode = (TreeNode)this;
				treeNode.updateData(treeNode.subNodes.get(0).getData());
			}
			
			TreeNode parentNode = this.getParent();
			if(parentNode == null){
				parentNode = new TreeNode(bTree);
				this.setParent(parentNode);
				parentNode.subNodes.add(this);
				newRightNode.setParent(parentNode);
				parentNode.subNodes.add(newRightNode);
				bTree.setTreeRoot(parentNode);
			} else if(parentNode.getSubNodes().size() < bTree.degree){
				newRightNode.setParent(parentNode);
				parentNode.addSubNode(parentNode.subNodes.indexOf(this) + 1, newRightNode);
			}else{
				parentNode.split(this, newRightNode);
			}
		}
	}
	
	static class TreeNode extends Node{
		private String data;
		
		void updateData(String data) {
			this.data = data;
			TreeNode parentNode = this.getParent();
			if(parentNode != null && parentNode.getSubNodes().indexOf(this) == 0){
				parentNode.updateData(data);
			}
		}

		@Override
		public String getData() {
			return data;
		}

		private ArrayList<Node> subNodes;
		
		public ArrayList<Node> getSubNodes() {
			return subNodes;
		}
		
		public TreeNode(BTree bTree){
			super(bTree);
			subNodes = new ArrayList<Node>(bTree.degree);
		}
		
		@Override
		public DataNode insert(DataNode dataNode){
			Node node = findNodeForSearchAndInsert(dataNode.getData());		
			return node.insert(dataNode);
		}

		@Override
		public DataNode search(String data) {
			Node node = findNodeForSearchAndInsert(data);		
			return node.search(data);
		}
		
		private Node findNodeForSearchAndInsert(String data){
			int index = 0;
			for(; index < this.subNodes.size(); index ++){			
				if(this.subNodes.get(index).getData().compareTo(data) > 0){
					break;
				}
			}
			
			if(index == 0){
				return this.subNodes.get(0);
			}else {
				return this.subNodes.get(index - 1);
			}
		}
		
		@Override
		public boolean isLeafNode(){
			return false;
		}
		
		public void split(Node currentSubNode, Node rightSubNode){
			int splitIndex = getSplitIndex();
			int indexOfCurrentSubNode = this.subNodes.indexOf(currentSubNode);
			TreeNode newRightNode = new TreeNode(getBTree());
			for(int i = splitIndex; i < this.subNodes.size(); i++){
				Node moveNode = this.subNodes.get(i);
				newRightNode.subNodes.add(moveNode);
				moveNode.setParent(newRightNode);
			}
		
			for(int i = this.subNodes.size() - 1; i >= splitIndex; i--){
				this.subNodes.remove(i);
			}
			
			if(indexOfCurrentSubNode < splitIndex){
				addSubNode(indexOfCurrentSubNode + 1, rightSubNode);
				rightSubNode.setParent(this);
			}else{
				newRightNode.addSubNode(indexOfCurrentSubNode - splitIndex + 1, rightSubNode);
				rightSubNode.setParent(newRightNode);
			}
			
			newRightNode.updateData(newRightNode.subNodes.get(0).getData());

			propagate(newRightNode);
		}
		
		protected void addSubNode(int index, Node node){
			if(index == this.subNodes.size()){
				this.subNodes.add(node);
			}else{
				this.subNodes.add(index, node);
			}
		}
 	}
	
	static class LeafNode extends Node{
		private DataNode[] dataNodes;
		private LeafNode nextNode;
		private int dataNodeSize = 0;

		@Override
		public String getData() {
			return dataNodes[0] == null ? null : dataNodes[0].getData();
		}
		
		public LeafNode(BTree bTree){
			super(bTree);
			dataNodes = new DataNode[getBTree().degree];
		}
		
		public LeafNode getNextNode() {
			return nextNode;
		}

		public void setNextNode(LeafNode nextNode) {
			this.nextNode = nextNode;
		}
		
		DataNode[] getDataNodesInternal() {
			return dataNodes;
		}
		
		public Collection<DataNode> getDataNodes(){
			ArrayList<DataNode> nodes = new ArrayList<DataNode>();
			for(int i=0; i< dataNodeSize; i++){
				nodes.add(dataNodes[i]);
			}
			return nodes;
		}
		
		public DataNode getLastDataNode(){
			return dataNodes[dataNodeSize - 1];
		}

		@Override
		public DataNode insert(DataNode dataNode) {
			Objects.requireNonNull(dataNode);
			DataNode existDataNode = search(dataNode.getData());
			if(existDataNode != null)
				return existDataNode;
			
			if(dataNodeSize == getBTree().degree){
				split(dataNode);
			}else{
				int insertIndex = findInsertIndex(dataNode);
				insertInternal(dataNode, insertIndex);
			}
			
			return dataNode;
		}
		
		public void insertInternal(DataNode dataNode, int insertIndex){
			if(insertIndex <= dataNodeSize - 1){
				// move the items backward.
				for(int i = dataNodeSize - 1; i >= insertIndex; i--){
					dataNodes[i+1] = dataNodes[i];
				}
				dataNodes[insertIndex] = dataNode;
			}else{
				dataNodes[dataNodeSize] = dataNode;
			}
			dataNodeSize += 1;
			dataNode.setParent(this);
			// update parent data if the first data node has been changed
			if(insertIndex == 0 && this.getParent() != null){
				this.getParent().updateData(dataNode.getData());
			}
		}
		
		private int findInsertIndex(DataNode dataNode){
			if(dataNodeSize == 0)
				return 0;
			
			if(dataNodeSize == 1){
				return dataNodes[0].data.compareTo(dataNode.data) > 0 ? 0 : 1;
			}
			
			int lowIndex = 0;
			int middleIndex = 0;
			int highIndex = dataNodeSize - 1;
			while(lowIndex + 1 < highIndex){
				middleIndex = (lowIndex + highIndex)/2;
				int compareResult = dataNodes[middleIndex].data.compareTo(dataNode.data);
				if(compareResult == 0){
					return middleIndex;
				}else if(compareResult > 0){
					highIndex = middleIndex;
				}else{
					lowIndex = middleIndex;
				}
			}
			
			// highIndex = lowIndex + 1
			if(highIndex != lowIndex + 1){
				System.out.println("Something wrong here highIndex != lowIndex + 1");
			}
			
			if(dataNodes[lowIndex].data.compareTo(dataNode.data) >= 0){
				return lowIndex;
			}else if(dataNodes[lowIndex].data.compareTo(dataNode.data) < 0 && dataNodes[highIndex].data.compareTo(dataNode.data) >= 0){
				return highIndex;
			}else{
				return highIndex + 1;
			}
		}
		
		public void split(DataNode dataNode){
			int splitIndex = getSplitIndex();
			LeafNode newRightNode = new LeafNode(getBTree());
			int rightSideSize = getSplitedRightSideSize();
			int insertIndex = findInsertIndex(dataNode);
			System.arraycopy(dataNodes, splitIndex, newRightNode.getDataNodesInternal(), 0, rightSideSize);
			dataNodeSize = splitIndex;
			newRightNode.dataNodeSize = rightSideSize;
			resetData(splitIndex, dataNodes.length);
			
			if(insertIndex < splitIndex){
				insertInternal(dataNode, insertIndex);
			}else{
				newRightNode.insertInternal(dataNode, insertIndex - splitIndex);
			}
			
			if(search(dataNode.getData()) == null && newRightNode.search(dataNode.getData()) == null){
				System.out.println("There is something wrong.");
			}
			
			newRightNode.setNextNode(getNextNode());
			this.setNextNode(newRightNode);
			propagate(newRightNode);
		}

		private void resetData(int startIndex, int endIndex){
			for(int i = startIndex; i < endIndex; i++){
				dataNodes[i] = null;
			}
		}
		
		@Override
		public DataNode search(String data) {
			for(int i=0;i < dataNodeSize; i++){
				if(this.dataNodes[i] == null)
					break;
				
				if(this.dataNodes[i].getData().compareTo(data) == 0){
					return this.dataNodes[i];
				}
			}
			return null;
		}
		
		@Override
		public boolean isLeafNode(){
			return true;
		}
	}
	
	public static class DataNode{
		private String data;
		// only for test
		private LeafNode parent;
		
		public DataNode(String data){
			this.data = data;
		}
		
		public String getData() {
			return data;
		}
		
		public void setParent(LeafNode node){
			this.parent = node;
		}
	}
}


