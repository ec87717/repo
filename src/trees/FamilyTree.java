package trees;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class FamilyTree<T> {

    private static class TreeNode<T> {
        private T data;
        private TreeNode<T> parent;
        private ArrayList<TreeNode<T>> children;

        TreeNode(T name) {
            this.data = name;
            children = new ArrayList<>();
        }

        T getData() {
            return data;
        }

        void addChild(TreeNode<T> childNode) {
            children.add(childNode);
            childNode.parent = this;
            // Add childNode to this node's children list. Also
            // set childNode's parent to this node.
        }

        // Searches subtree at this node for a node
        // with the given name. Returns the node, or null if not found.
        TreeNode<T> getNodeWithName(String targetName) {
            // Does this node have the target name?
            if (data.equals(targetName))
                return this;
                    
            // No, recurse. Check all children of this node.
            for (TreeNode<T> child: children)
            {
                TreeNode<T> find = child.getNodeWithName(targetName);
                if (find != null) {
                    return find;
                }
                // If child.getNodeWithName(targetName) returns a non-null node,
                // then that's the node we're looking for. Return it.
            }
            
            // Not found anywhere.
            return null;
        }

        // Returns a list of ancestors of this TreeNode<T>, starting with this node’s
        // parent and
        // ending with the root. Order is from recent to ancient.
        ArrayList<TreeNode<T>> collectAncestorsToList() {
            ArrayList<TreeNode<T>> ancestors = new ArrayList<>();

            TreeNode<T> ances = parent;

            while (ances != null) {
                ancestors.add(ances);
                ances = ances.parent;
            }

            return ancestors;
        }

        public String toString() {
            return toStringWithIndent("");
        }

        private String toStringWithIndent(String indent) {
            String s = indent + data + "\n";
            indent += "  ";
            for (TreeNode<T> childNode : children)
                s += childNode.toStringWithIndent(indent);
            return s;
        }
    }

    private TreeNode<T> root;

    //
    // Displays a file browser so that user can select the family tree file.
    //
    public FamilyTree() throws IOException, TreeException {
        // User chooses input file. This block doesn't need any work.
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Family tree text files", "txt");
        File dirf = new File("data");
        if (!dirf.exists()) dirf = new File(".");

        JFileChooser chooser = new JFileChooser(dirf);
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) System.exit(1);
        File treeFile = chooser.getSelectedFile();

        // Parse the input file. Create a FileReader that reads treeFile. Create a BufferedReader
        // that reads from the FileReader.
        FileReader fr = new FileReader(treeFile);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null)
            addLine(line);
        br.close();
        fr.close();
    }

    //
    // Line format is "parent:child1,child2 ..."
    // Throws TreeException if line is illegal.
    //
    private void addLine(String line) throws TreeException
    {
        // Extract parent and array of children.
        int colonIndex = line.indexOf(':'); 
        if (colonIndex < 0)  throw new TreeException("No colon");
        String parent = line.substring(0,colonIndex); 
        String childrenString = line.substring(colonIndex+1);
        String[] childrenArray = childrenString.split(",");        
        // Find parent node. If root is null then the tree is empty and the
        // parent node must be constructed. Otherwise the parent node should be 
        // somewhere in the tree.
        TreeNode<T> parentNode;
        if (root == null)
            parentNode = root = new TreeNode(parent);
        else
        {
            parentNode = root.getNodeWithName(parent);  /*There's a method in Node that searches for a named node. 
            ??? If the parent node wasn't found, there must have been something wrong in the 
                data file. Throw an exception.*/
            if (parentNode == null)
                throw new TreeException("Orphan");
        }
        
        // Add child nodes to parentNode.
        for (String child : childrenArray) {
            TreeNode<T> nodes = new TreeNode(child);
            parentNode.addChild(nodes);
        }
    }

    // Returns the "deepest" node that is an ancestor of the node named name1, and
    // also is an
    // ancestor of the node named name2.
    //
    // "Depth" of a node is the "distance" between that node and the root. The depth
    // of the root is 0. The
    // depth of the root's immediate children is 1, and so on.
    //
    TreeNode<T> getMostRecentCommonAncestor(String name1, String name2) throws TreeException
    {
        // Get nodes for input names.
        TreeNode<T> node1 = root.getNodeWithName(name1);        // node whose name is name1
        if (node1 == null)
            throw new TreeException("No name1 found");
        TreeNode<T> node2 = root.getNodeWithName(name2);        // node whose name is name2
        if (node2 == null)
            throw new TreeException("No name2 found");
        
        // Get ancestors of node1 and node2.
        ArrayList<TreeNode<T>> ancestorsOf1 = node1.collectAncestorsToList();
        ArrayList<TreeNode<T>> ancestorsOf2 = node2.collectAncestorsToList();
        
        // Check members of ancestorsOf1 in order until you find a node that is also
        // an ancestor of 2. 
        for (TreeNode<T> n1: ancestorsOf1)
            if (ancestorsOf2.contains(n1))
                return n1;
        
        // No common ancestor.
        return null;
    }

    public String toString() {
        return "Family Tree:\n\n" + root;
    }

    public static void main(String[] args) {
        try {
            FamilyTree tree = new FamilyTree();
            System.out.println("Tree:\n" + tree + "\n**************\n");
            TreeNode ancestor = tree.getMostRecentCommonAncestor("Bilbo", "Frodo");
            System.out.println("Most recent common ancestor of Bilbo and Frodo is " + ancestor.getData());
        } catch (IOException x) {
            System.out.println("IO trouble: " + x.getMessage());
        } catch (TreeException x) {
            System.out.println("Input file trouble: " + x.getMessage());
        }
    }
}
