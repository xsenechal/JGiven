package com.tngtech.jgiven.impl.intercept;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import com.tngtech.jgiven.impl.intercept.StepInterceptor.Invoker;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.BindingPriority;
import net.bytebuddy.implementation.bind.annotation.DefaultCall;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * StepInterceptorImpl that uses ByteBuddy Method interceptor with annotations for intercepting JGiven methods
 *
 */
public class ByteBuddyMethodInterceptor {

    /**
     * The interceptor to delegate method calls.
     * Can be {@code null} in some cases until the scenario has started
     */
    private StepInterceptor interceptor;

    public ByteBuddyMethodInterceptor() {}

    public ByteBuddyMethodInterceptor( StepInterceptor interceptor ) {
        this.interceptor = interceptor;
    }

    public void setInterceptor( StepInterceptor interceptor ) {
        this.interceptor = interceptor;
    }

    @RuntimeType
    @BindingPriority( BindingPriority.DEFAULT * 3 )
    public Object interceptSuper( @SuperCall final Callable<?> zuper, @This final Object receiver, @Origin Method method,
            @AllArguments final Object[] parameters )
            throws Throwable {

        if( handleSetStepInterceptor( method, parameters ) ) {
            return null;
        }

        if (interceptor == null) {
            return zuper.call();
        }

        Invoker invoker = new Invoker() {
            @Override
            public Object proceed() throws Throwable {
                return zuper.call();
            }

        };
        return interceptor.intercept( receiver, method, parameters, invoker );
    }

    @RuntimeType
    @BindingPriority( BindingPriority.DEFAULT * 2 )
    public Object interceptDefault( @DefaultCall final Callable<?> zuper, @This final Object receiver, @Origin Method method,
            @AllArguments final Object[] parameters )
            throws Throwable {

        if( handleSetStepInterceptor( method, parameters ) ) {
            return null;
        }

        if (interceptor == null) {
            return zuper.call();
        }

        Invoker invoker = new Invoker() {
            @Override
            public Object proceed() throws Throwable {
                return zuper.call();
            }

        };
        return interceptor.intercept( receiver, method, parameters, invoker );
    }

    @RuntimeType
    public Object intercept( @This final Object receiver, @Origin final Method method, @AllArguments final Object[] parameters )
            throws Throwable {
        // this intercepted method does not have a non-abstract super method

        if( handleSetStepInterceptor( method, parameters ) ) {
            return null;
        }

        if (interceptor == null) {
            return null;
        }

        Invoker invoker = new Invoker() {

            @Override
            public Object proceed() throws Throwable {
                return null;
            }

        };
        return interceptor.intercept( receiver, method, parameters, invoker );
    }

    private boolean handleSetStepInterceptor( Method method, final Object[] parameters ) {
        if( method.getName().equals( "setStepInterceptor" ) && method.getDeclaringClass().equals( StageInterceptorInternal.class ) ) {
            setInterceptor( (StepInterceptor) parameters[0] );
            return true;
        }
        return false;
    }

}
