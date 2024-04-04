import React, { useState, useEffect, useCallback, FC } from 'react';
import './styles/Slider.css';
import {TransitionGroup, CSSTransition} from "react-transition-group";

/**
 * Слайдер изобажений блюд
 * @constructor
 */
const Slider: FC = () => {
    const [currentIndex, setCurrentIndex] = useState<number>(0);
    const [userActive, setUserActive] = useState<boolean>(false);
    const slides: string[] = [
        'https://tvil.ru/blog/sites/blog/files/image/ckeditor/kulinarnie_turi_v_gr.jpg',
        'https://static.tildacdn.com/tild6264-3132-4630-b937-663332313063/photo.jpg',
        'http://папамихо.рф/wp-content/uploads/2020/04/244_402_20210922160529_2850-1536x1024.jpg',

    ];

    const goToPrevSlide = () => {
        setCurrentIndex((prevIndex: number): number =>
            prevIndex === 0 ? slides.length - 1 : prevIndex - 1
        );
        setUserActive(true);
    };

    const goToNextSlide = useCallback(() => {
        setCurrentIndex((prevIndex: number): number =>
            prevIndex === slides.length - 1 ? 0 : prevIndex + 1
        );
        setUserActive(true);
    }, [slides.length]);

    useEffect(() => {
        let interval: NodeJS.Timeout;

        if (!userActive) {
            interval = setInterval(goToNextSlide, 100000000);
        }

        return () => clearInterval(interval);
    }, [userActive, goToNextSlide]);

    useEffect(() => {
        const timeout = setTimeout(() => {
            setUserActive(false);
        }, 1000);

        return () => clearTimeout(timeout);
    }, [currentIndex]);

    return (
        <div className="slider">
            <button className="slider-button prev" onClick={goToPrevSlide}>
                &lt;
            </button>
            <TransitionGroup className="slider-wrapper">
                <CSSTransition
                    key={currentIndex}
                    timeout={1000}
                    classNames="slide"
                    unmountOnExit
                >
                    <div className="slider-slide">
                        <img
                            className="slider-img"
                            src={slides[currentIndex]}
                            alt={`Slide ${currentIndex + 1}`}
                        />
                    </div>
                </CSSTransition>
            </TransitionGroup>
            <button className="slider-button next" onClick={goToNextSlide}>
                &gt;
            </button>
        </div>
    );
};

export default Slider;

