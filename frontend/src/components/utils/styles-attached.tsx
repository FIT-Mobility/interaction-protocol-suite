import withStyles, { StyleRules, StyleRulesCallback, WithStyles } from '@material-ui/core/es/styles/withStyles';
import classNames from 'classnames';
import * as React from 'react';

/**
 * Attaches JSS styles to a component.
 *
 * @param Component The component to be decorated with JSS styles.
 * @param styles The JSS styles object / function. The `root` style is attached to
 *  the decorated component itself, while all others are passed to the component
 *  via the `classes` prop.
 */
export function attachStyles<P, ClassKey extends string>(
    Component: React.ComponentType<P>,
    styles: StyleRulesCallback<ClassKey> | StyleRules<ClassKey>,
) {
    type Props = P & WithStyles<typeof styles>;

    const AttachedStylesInner: React.SFC<Props> = props => {
        const { className, classes, innerRef, ...rest } = props as any;
        const { root, ...restClasses } = classes as any;

        return (
            <Component
                className={classNames(className, root)}
                classes={restClasses}
                ref={innerRef}
                {...rest}
            />
        );
    };

    return withStyles(styles)(AttachedStylesInner as any) as React.ComponentType<P>;
}
