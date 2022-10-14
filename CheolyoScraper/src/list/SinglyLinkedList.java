package list;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A singly-linked list is a linked-memory representation of the List abstract
 * data type. This list maintains a dummy/sentinel front node in the list to
 * help promote cleaner implementations of the list behaviors. This list also
 * maintains a reference to the tail/last node in the list at all times to
 * ensure O(1) worst-case cost for adding to the end of the list. Size is
 * maintained as a global field to allow for O(1) size() and isEmpty()
 * behaviors.
 * 
 * @author Dr. King
 *
 * @param <E> the type of elements stored in the list
 */
public class SinglyLinkedList<E> extends AbstractList<E> {

	/** A reference to the dummy/sentinel node at the front of the list **/
	private LinkedListNode<E> front;

	/** A reference to the last/final node in the list **/
	private LinkedListNode<E> tail;

	/** The number of elements stored in the list **/
	private int size;

	/**
	 * Constructs an empty singly-linked list
	 */
	public SinglyLinkedList() {
		front = new LinkedListNode<E>(null);
		tail = null;
		size = 0;
	}

	@Override
	public void add(int index, E element) {
		checkIndexForAdd(index);
		if (size == 0) {
			front.setNext(new LinkedListNode<E>(element));
			tail = front.getNext();
		} else if (index == size) {
			tail.setNext(new LinkedListNode<E>(element));
			tail = tail.getNext();
		} else {
			LinkedListNode<E> temp = front;
			int intCounter = 0;
			while (index != intCounter++) {
				temp = temp.getNext();
			}
			temp.setNext(new LinkedListNode<E>(element, temp.getNext()));
		}
		size++;
	}

	@Override
	public E get(int index) {
		checkIndex(index);
		if (index == size - 1) {
			return tail.getElement();
		}
		LinkedListNode<E> temp = front;
		int intCounter = 0;
		while (index + 1 != intCounter++) {
			temp = temp.getNext();
		}
		return temp.getElement();
	}

	@Override
	public E remove(int index) {
		checkIndex(index);
		LinkedListNode<E> temp = front;
		size--;
		E eReturn;
		if (index == 0) {
			eReturn = front.getNext().getElement();
			front.setNext(front.getNext().getNext());
			return eReturn;
		}
		int intCounter = 0;
		while (index != intCounter++) {
			temp = temp.getNext();
		}
		eReturn = temp.getNext().getElement();
		if (index == size) {
			tail = temp;
		}
		temp.setNext(temp.getNext().getNext());
		return eReturn;
	}

	@Override
	public E set(int index, E element) {
		checkIndex(index);
		E eReturn;
		if (index == size - 1) {
			eReturn = tail.getElement();
			tail.setElement(element);
			return eReturn;
		} else if (index == 0) {
			eReturn = front.getNext().getElement();
			front.getNext().setElement(element);
			return eReturn;
		}
		int intCounter = 0;
		LinkedListNode<E> temp = front;
		while (index + 1 != intCounter++) {
			temp = temp.getNext();
		}
		eReturn = temp.getElement();
		temp.setElement(element);
		return eReturn;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Iterator<E> iterator() {
		return new ElementIterator();
	}

	/**
	 * {@inheritDoc} For a singly-linked list, this behavior has O(1) worst-case
	 * runtime.
	 */
	@Override
	public E last() {
		if (isEmpty()) {
			throw new IndexOutOfBoundsException("The list is empty");
		}
		return tail.getElement();
	}

	/**
	 * {@inheritDoc} For this singly-linked list, addLast(element) behavior has O(1)
	 * worst-case runtime.
	 */
	@Override
	public void addLast(E element) {
		if (tail == null) {
			add(0, element);
		} else {
			tail.setNext(new LinkedListNode<E>(element));
			tail = tail.getNext();
			size++;
		}
	}

	/**
	 * Node utilized by the SinglyLinkedList.
	 * 
	 * @author Alexander Chen.
	 *
	 * @param <E> the type of element stored in the node
	 */
	private static class LinkedListNode<E> {

		/** Data stored within the node. */
		private E data;
		/** Next node in line relative to this node. */
		private LinkedListNode<E> next;

		/**
		 * Creates a Node object, but without a next object.
		 * 
		 * @param data Data stored within the node.
		 */
		public LinkedListNode(E data) {
			super();
			this.data = data;
		}

		/**
		 * Constructor.
		 * 
		 * @param data Data stored within the node.
		 * @param next Next node in line relative to this node.
		 */
		public LinkedListNode(E data, LinkedListNode<E> next) {
			super();
			this.data = data;
			this.next = next;
		}

		/**
		 * Returns the element of the node.
		 * 
		 * @return the element of the node.
		 */
		public E getElement() {
			return data;
		}

		/**
		 * Sets the element of the node.
		 * 
		 * @param data the data to set
		 */
		public void setElement(E data) {
			this.data = data;
		}

		/**
		 * Returns the next node.
		 * 
		 * @return the next
		 */
		public LinkedListNode<E> getNext() {
			return next;
		}

		/**
		 * Returns the next node.
		 * 
		 * @param next The next node to set
		 */
		public void setNext(LinkedListNode<E> next) {
			this.next = next;
		}
	}

	/**
	 * An Iterator that iterates through the elements of this type of List.
	 * 
	 * @author Alexander Chen
	 */
	private class ElementIterator implements Iterator<E> {
		/**
		 * Keep track of the next node that will be processed
		 */
		private LinkedListNode<E> current;

		/**
		 * Keep track of the node that was processed on the last call to 'next'
		 */
		private LinkedListNode<E> previous;

		/**
		 * Keep track of the previous-previous node that was processed so that we can
		 * update 'next' links when removing
		 */
		private LinkedListNode<E> previousPrevious;

		/**
		 * Keep track of whether it's ok to remove an element (based on whether next()
		 * has been called immediately before remove())
		 */
		private boolean removeOK;

		/**
		 * Construct a new element iterator where the cursor is initialized to the
		 * beginning of the list.
		 */
		public ElementIterator() {
			previousPrevious = front;
			previous = front;
			current = front.getNext();
		}

		@Override
		public boolean hasNext() {
			return current != null;
		}

		@Override
		public E next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			E eReturn = current.getElement();
			current = current.getNext();
			previous = previous.getNext();
			if (previousPrevious.getNext() != previous && previousPrevious.getNext() != current) {
				previousPrevious = previousPrevious.getNext();
			}
			removeOK = true;
			return eReturn;
		}

		@Override
		public void remove() {
			if (!removeOK) {
				throw new IllegalStateException();
			}
			previousPrevious.setNext(current);
			previous = previousPrevious;
			removeOK = false;
			size--;
			if (current == null) {
				tail = previous;
			}
		}
	}
}
