package com.tngtech.jgiven.impl.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.tngtech.jgiven.annotation.BeforeStage;
import com.tngtech.jgiven.exception.JGivenExecutionException;
import com.tngtech.jgiven.exception.JGivenInjectionException;
import com.tngtech.jgiven.impl.util.ReflectionUtil.MethodAction;

public class ReflectionUtilTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    static class TestClass {
        private String testField;

        private void testMethod( Integer someArg ) {}
    }

    @Test
    public void injection_exception_is_thrown_if_field_cannot_be_set() throws Exception {
        expectedException.expect( JGivenInjectionException.class );
        ReflectionUtil.setField( TestClass.class.getDeclaredField( "testField" ), new TestClass(), 5, "test description" );
    }

    @Test
    public void makeAccessible_does_not_throw_execptions() throws Exception {
        AccessibleObject stub = new AccessibleObject() {
            @Override
            public void setAccessible( boolean flag ) throws SecurityException {
                throw new SecurityException();
            }
        };
        ReflectionUtil.makeAccessible( stub, "test" );
    }

    @Test
    public void execution_exception_is_thrown_if_method_cannot_be_invoked() throws Exception {
        expectedException.expect( JGivenExecutionException.class );
        TestClass testClass = new TestClass();
        ReflectionUtil.invokeMethod( testClass, TestClass.class.getDeclaredMethod( "testMethod", Integer.class ), "test description" );
    }

    static class ChildClass extends ParentClass {

        @BeforeStage
        void m1() {

        }

        @Override
        @BeforeStage
        void m2() {

        }

        @BeforeStage
        void m3( String arg ) {

        }

        @BeforeStage
        void m4( Integer i ) {

        }

        @Override
        @BeforeStage
        void m5( String s ) {

        }
    }

    static class ParentClass {
        @BeforeStage
        void m2() {

        }

        @BeforeStage
        void m3() {

        }

        @BeforeStage
        void m4( String s ) {

        }

        @BeforeStage
        void m5( String s ) {

        }
    }

    @Test
    public void overridden_methods_are_only_called_once() {
        final List<String> methodsCalled = Lists.newArrayList();
        ReflectionUtil.forEachMethod( new ChildClass(), ChildClass.class, BeforeStage.class, new MethodAction() {
            @Override
            public void act( Object object, Method method ) throws Exception {
                methodsCalled.add( method.getName() );
            }
        } );

        Assertions.assertThat( methodsCalled ).containsExactly( "m1", "m2", "m3", "m4", "m5", "m3", "m4" );

    }

}
