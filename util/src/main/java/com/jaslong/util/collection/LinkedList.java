package com.jaslong.util.collection;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedList<E> implements Deque<E> {

    public static class Node<E> {

        private final LinkedList<E> mList;
        private final E mValue;
        private boolean mIsAttached;
        @Nullable private Node<E> mPrevious;
        @Nullable private Node<E> mNext;

        public Node(LinkedList<E> list, E value) {
            mList = list;
            mValue = value;
        }

        public E getValue() {
            return mValue;
        }

        public E remove() throws DetachedException {
            return mList.remove(this);
        }

    }

    private static class LinkedListIterator<E> implements Iterator<E> {

        private LinkedList<E> mList;
        private Node<E> mNextNode;

        public LinkedListIterator(LinkedList<E> list) {
            mList = list;
            mNextNode = list.mFirst;
        }

        @Override
        public boolean hasNext() {
            return mNextNode != null;
        }

        @Override
        public E next() {
            E e = mNextNode.mValue;
            mNextNode = mNextNode.mNext;
            return e;
        }

        @Override
        public void remove() {
            if (mNextNode.mPrevious == null) {
                throw new IllegalStateException();
            }
            mList.remove(mNextNode.mPrevious);
        }
    }

    private static class DescendingLinkedListIterator<E> implements Iterator<E> {

        private LinkedList<E> mList;
        private Node<E> mNextNode;

        public DescendingLinkedListIterator(LinkedList<E> list) {
            mList = list;
            mNextNode = list.mLast;
        }

        @Override
        public boolean hasNext() {
            return mNextNode != null;
        }

        @Override
        public E next() {
            E e = mNextNode.mValue;
            mNextNode = mNextNode.mPrevious;
            return e;
        }

        @Override
        public void remove() {
            if (mNextNode.mNext == null) {
                throw new IllegalStateException();
            }
            mList.remove(mNextNode.mNext);
        }
    }

    public static class DetachedException extends RuntimeException {
        public DetachedException() {
            super();
        }
        public DetachedException(String detailMessage) {
            super(detailMessage);
        }
        public DetachedException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }
        public DetachedException(Throwable throwable) {
            super(throwable);
        }
    }

    private Node<E> mFirst;
    private Node<E> mLast;
    private int mSize;

    public LinkedList() {
        mFirst = null;
        mLast = null;
        mSize = 0;
    }

    public Node<E> addFirstGetNode(E e) {
        Node<E> newNode = new Node<>(this, e);
        if (mFirst == null) {
            mFirst = newNode;
            mLast = newNode;
        } else {
            mFirst.mPrevious = newNode;
            mFirst = newNode;
        }
        newNode.mIsAttached = true;
        ++mSize;
        return newNode;
    }

    public Node<E> addLastGetNode(E e) {
        Node<E> newNode = new Node<>(this, e);
        if (mLast == null) {
            mFirst = newNode;
            mLast = newNode;
        } else {
            mLast.mNext = newNode;
            mLast = newNode;
        }
        newNode.mIsAttached = true;
        ++mSize;
        return newNode;
    }

    public Node<E> addBefore(Node<E> node, E e) throws DetachedException {
        if (!node.mIsAttached) {
            throw new DetachedException();
        }
        Node<E> newNode = new Node<>(this, e);
        newNode.mPrevious = node.mPrevious;
        newNode.mNext = node;
        if (node.mPrevious != null) {
            node.mPrevious.mNext = newNode;
        }
        node.mPrevious = newNode;
        newNode.mIsAttached = true;
        ++mSize;
        return newNode;
    }

    public Node<E> set(Node<E> node, E e) throws DetachedException {
        if (!node.mIsAttached) {
            throw new DetachedException();
        }
        Node<E> newNode = new Node<>(this, e);
        newNode.mPrevious = node.mPrevious;
        newNode.mNext = node.mNext;
        if (newNode.mPrevious != null) {
            newNode.mPrevious.mNext = newNode;
        }
        if (newNode.mNext != null) {
            newNode.mNext.mPrevious = newNode;
        }
        newNode.mIsAttached = true;
        node.mIsAttached = false;
        return newNode;
    }

    public E remove(Node<E> node) throws DetachedException {
        if (!node.mIsAttached) {
            throw new DetachedException();
        }
        if (node.mPrevious != null) {
            node.mPrevious.mNext = node.mNext;
        }
        if (node.mNext != null) {
            node.mNext.mPrevious = node.mPrevious;
        }
        node.mIsAttached = false;
        --mSize;
        return node.mValue;
    }

    private void throwIfEmpty() {
        if (mFirst == null) {
            throw new NoSuchElementException("LinkedList is empty!");
        }
    }

    private boolean isEqual(Object o1, Object o2) {
        return o1 == o2 || (o1 != null && o1.equals(o2));
    }

    private Node<E> indexOfGetNode(Object o) {
        Node<E> node = mFirst;
        while (node != null) {
            if (isEqual(o, node.mValue)) {
                return node;
            }
            node = node.mNext;
        }
        return null;
    }

    private Node<E> lastIndexOfGetNode(Object o) {
        Node<E> node = mLast;
        while (node != null) {
            if (isEqual(o, node.mValue)) {
                return node;
            }
            node = node.mPrevious;
        }
        return null;
    }

    // START NON-IMPORTANT METHODS

    @Override
    public void addFirst(E e) {
        addFirstGetNode(e);
    }

    @Override
    public void addLast(E e) {
        addLastGetNode(e);
    }

    @Override
    public boolean offerFirst(E e) {
        addFirstGetNode(e);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        addLastGetNode(e);
        return false;
    }

    @Override
    public E removeFirst() {
        throwIfEmpty();
        return remove(mFirst);
    }

    @Override
    public E removeLast() {
        throwIfEmpty();
        return remove(mLast);
    }

    @Override
    public E pollFirst() {
        return mFirst == null ? null : remove(mFirst);
    }

    @Override
    public E pollLast() {
        return mLast == null ? null : remove(mLast);
    }

    @Override
    public E getFirst() {
        throwIfEmpty();
        return mFirst.mValue;
    }

    @Override
    public E getLast() {
        throwIfEmpty();
        return mFirst.mValue;
    }

    @Override
    public E peekFirst() {
        return mFirst != null ? mFirst.mValue : null;
    }

    @Override
    public E peekLast() {
        return mLast != null ? mLast.mValue : null;
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        Node<E> node = indexOfGetNode(o);
        if (node != null) {
            remove(node);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        Node<E> node = lastIndexOfGetNode(o);
        if (node != null) {
            remove(node);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean add(E e) {
        addLast(e);
        return true;
    }

    @Override
    public boolean offer(E e) {
        return offerLast(e);
    }

    @Override
    public E remove() {
        return removeFirst();
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public E peek() {
        return peekFirst();
    }

    @Override
    public void push(E e) {
        addFirst(e);
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    @Override
    public boolean contains(Object o) {
        for (E e : this) {
            if (isEqual(o, e)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return mSize;
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return new LinkedListIterator<>(this);
    }

    @NonNull
    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingLinkedListIterator<>(this);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        for (E e : collection) {
            add(e);
        }
        return true;
    }

    @Override
    public void clear() {
        while (mSize > 0) {
            remove();
        }
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return mSize == 0;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        boolean changed = false;
        for (Object o : collection) {
            changed |= remove(o);
        }
        return changed;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        boolean changed = false;
        Iterator<E> iterator = iterator();
        while (iterator.hasNext()) {
            if (!collection.contains(iterator.next())) {
                iterator.remove();
                changed = true;
            }
        }
        return changed;
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return toArray(new Object[mSize]);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] array) {
        int index = 0;
        for (E e : this) {
            array[index++] = (T) e;
        }
        return array;
    }

}
